package com.musicdreamer.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.musicdreamer.service.IndexSyncService;
import com.musicdreamer.entity.SongDoc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Canal Binlog 监听器
 * 自动同步 MySQL song 表变更到 Elasticsearch
 *
 * 注意：binlog 只携带 song 表的列，singerName / lyrics 需要回查 MySQL 补全，
 * 否则 ES 文档中这些字段为空，搜索不到歌手名和歌词。
 */
@Slf4j
@Component
public class CanalSyncConfig {

    private final IndexSyncService indexSyncService;
    private final JdbcTemplate jdbcTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "canal-sync");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CanalSyncConfig(IndexSyncService indexSyncService, JdbcTemplate jdbcTemplate) {
        this.indexSyncService = indexSyncService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 根据 song_id 回查 MySQL，补全 singerName / lyrics 等 binlog 中不携带的字段 */
    private SongDoc enrichFromDb(Long songId) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT s.song_id, s.name, IFNULL(si.name,'') AS singer_name, " +
                "       IFNULL(s.lyrics,'') AS lyrics, IFNULL(s.genre,'') AS genre, " +
                "       IFNULL(s.language,'') AS language, IFNULL(s.play_count,0) AS play_count, " +
                "       DATE_FORMAT(s.release_date,'%Y-%m-%d') AS release_date " +
                "FROM song s LEFT JOIN singer si ON s.singer_id = si.singer_id " +
                "WHERE s.song_id = ?",
                (rs, rowNum) -> {
                    SongDoc doc = new SongDoc();
                    doc.setSongId(rs.getLong("song_id"));
                    doc.setName(rs.getString("name"));
                    doc.setSingerName(rs.getString("singer_name"));
                    doc.setLyrics(rs.getString("lyrics"));
                    doc.setGenre(rs.getString("genre"));
                    doc.setLanguage(rs.getString("language"));
                    doc.setPlayCount(rs.getInt("play_count"));
                    // release_date 为 NULL 时返回 null（而非空字符串），避免 ES Date 字段解析失败
                    doc.setReleaseDate(rs.getString("release_date"));
                    return doc;
                }, songId);
        } catch (Exception e) {
            log.warn("[Canal] enrich failed for songId={}: {}", songId, e.getMessage());
            return null;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startCanalListener() {
        if (!running.compareAndSet(false, true)) {
            log.info("[Canal] listener already running, skip");
            return;
        }
        scheduler.execute(this::runCanalLoop);
    }

    private void runCanalLoop() {
        while (running.get()) {
            CanalConnector connector = null;
            try {
                String host = System.getenv().getOrDefault("CANAL_HOST", "localhost");
                int port = Integer.parseInt(System.getenv().getOrDefault("CANAL_PORT", "11111"));
                String destination = System.getenv().getOrDefault("CANAL_DESTINATION", "music_dreamer");
                String username = System.getenv().getOrDefault("CANAL_USERNAME", "");
                String password = System.getenv().getOrDefault("CANAL_PASSWORD", "");

                connector = CanalConnectors.newSingleConnector(
                        new InetSocketAddress(host, port), destination, username, password);
                connector.connect();
                // 仅订阅 song 表，避免处理无关 binlog 事件浪费 CPU/IO
                connector.subscribe("music_dreamer.song");
                connector.rollback();
                log.info("[Canal] connected to {}:{}, subscribed to music_dreamer.song", host, port);

                while (running.get()) {
                    var messages = connector.getWithoutAck(100, 100L, TimeUnit.MILLISECONDS);
                    long batchId = messages.getId();
                    if (batchId == -1 || messages.getEntries().isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }
                    // 单行处理失败不应阻塞整个 batch 的 ack，避免毒消息导致无限重放
                    for (var entry : messages.getEntries()) {
                        if (entry.getEntryType() == com.alibaba.otter.canal.protocol.CanalEntry.EntryType.ROWDATA) {
                            try {
                                var rowChange = com.alibaba.otter.canal.protocol.CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                                for (var rowData : rowChange.getRowDatasList()) {
                                    if (rowChange.getEventType() == com.alibaba.otter.canal.protocol.CanalEntry.EventType.INSERT
                                            || rowChange.getEventType() == com.alibaba.otter.canal.protocol.CanalEntry.EventType.UPDATE) {
                                        var idCol = rowData.getAfterColumnsList().stream()
                                                .filter(c -> c.getName().equals("song_id"))
                                                .findFirst();
                                        if (idCol.isPresent()) {
                                            Long songId = Long.parseLong(idCol.get().getValue());
                                            SongDoc song = enrichFromDb(songId);
                                            if (song != null) indexSyncService.indexSong(song);
                                        }
                                    } else if (rowChange.getEventType() == com.alibaba.otter.canal.protocol.CanalEntry.EventType.DELETE) {
                                        var idCol = rowData.getBeforeColumnsList().stream()
                                                .filter(c -> c.getName().equals("song_id"))
                                                .findFirst();
                                        if (idCol.isPresent()) {
                                            indexSyncService.deleteSong(Long.parseLong(idCol.get().getValue()));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("[Canal] failed to process entry, skip: {}", e.getMessage());
                            }
                        }
                    }
                    connector.ack(batchId);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.info("[Canal] listener interrupted");
                break;
            } catch (Exception e) {
                log.error("[Canal] connection error, retry in 10s: {}", e.getMessage());
            } finally {
                if (connector != null) {
                    try { connector.disconnect(); } catch (Exception ignored) {}
                }
            }
            // 连接断开后等待重连，避免疯狂重试
            if (running.get()) {
                try { Thread.sleep(10_000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
            }
        }
        log.info("[Canal] listener stopped");
    }
}
