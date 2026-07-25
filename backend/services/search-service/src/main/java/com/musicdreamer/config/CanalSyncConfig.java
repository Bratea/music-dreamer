package com.musicdreamer.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.musicdreamer.service.IndexSyncService;
import com.musicdreamer.entity.SongDoc;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Canal Binlog 监听器
 * 自动同步 MySQL song 表变更到 Elasticsearch
 *
 * 注意：binlog 只携带 song 表的列，singerName / lyrics 需要回查 MySQL 补全，
 * 否则 ES 文档中这些字段为空，搜索不到歌手名和歌词。
 */
@Component
public class CanalSyncConfig {

    private final IndexSyncService indexSyncService;
    private final JdbcTemplate jdbcTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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
                "       IFNULL(DATE_FORMAT(s.release_date,'%Y-%m-%d'),'') AS release_date " +
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
                    doc.setReleaseDate(rs.getString("release_date"));
                    return doc;
                }, songId);
        } catch (Exception e) {
            System.err.println("[Canal] enrich failed for songId=" + songId + ": " + e.getMessage());
            return null;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startCanalListener() {
        String host = System.getenv().getOrDefault("CANAL_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("CANAL_PORT", "11111"));
        String destination = System.getenv().getOrDefault("CANAL_DESTINATION", "music_dreamer");
        String username = System.getenv().getOrDefault("CANAL_USERNAME", "");
        String password = System.getenv().getOrDefault("CANAL_PASSWORD", "");

        scheduler.scheduleAtFixedRate(() -> {
            CanalConnector connector = null;
            try {
                connector = CanalConnectors.newSingleConnector(
                        new InetSocketAddress(host, port), destination, username, password);
                connector.connect();
                connector.subscribe(".*\\..*");
                connector.rollback();

                while (true) {
                    var messages = connector.getWithoutAck(100, 100L, TimeUnit.MILLISECONDS);
                    long batchId = messages.getId();
                    if (batchId == -1 || messages.getEntries().isEmpty()) {
                        Thread.sleep(1000);
                        continue;
                    }
                    for (var entry : messages.getEntries()) {
                        if (entry.getEntryType() == com.alibaba.otter.canal.protocol.CanalEntry.EntryType.ROWDATA) {
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
                        }
                    }
                    connector.ack(batchId);
                }
            } catch (Exception e) {
                System.err.println("[Canal] Error: " + e.getMessage());
            } finally {
                if (connector != null) connector.disconnect();
                try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}
