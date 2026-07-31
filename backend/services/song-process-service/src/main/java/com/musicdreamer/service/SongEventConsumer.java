package com.musicdreamer.service;

import com.musicdreamer.entity.SongPublishEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 歌曲事件消费者 — 异步处理歌曲发布后的后续任务
 * 实际生产环境可扩展：
 *   1. 调用 FFmpeg 转码
 *   2. 上传到 OSS（阿里云OSS / MinIO）
 *   3. 同步更新 ES 索引
 *   4. 发送粉丝通知（通过 playlist-service 的消息接口）
 */
@Slf4j
@Service
public class SongEventConsumer {

    @RabbitListener(queues = com.musicdreamer.config.RabbitMQConfig.QUEUE_SONG_PUBLISH)
    public void onSongPublished(SongPublishEvent event) {
        log.info("[MQ] 收到歌曲发布事件: songId={} name={}", event.getSongId(), event.getName());
        try {
            // ── 步骤1: 模拟异步转码（实际可调用 FFmpeg） ──
            log.info("[MQ] 开始转码处理: songId={}", event.getSongId());
            // TimeUnit.SECONDS.sleep(2);  // 模拟耗时

            // ── 步骤2: 模拟上传 OSS ──
            log.info("[MQ] OSS上传完成: songId={}", event.getSongId());

            // ── 步骤3: 更新 ES 索引 ──
            // search-service 可通过 OpenFeign 调用，或直接发消息让 search-service 消费
            log.info("[MQ] ES 索引已更新: songId={}", event.getSongId());

            // ── 步骤4: 发送歌手发歌通知 ──
            log.info("[MQ] 粉丝通知已推送: songId={}", event.getSongId());

            log.info("[MQ] 歌曲处理完成: songId={}", event.getSongId());
        } catch (Exception e) {
            log.error("[MQ] 处理歌曲事件失败: songId={} error={}", event.getSongId(), e.getMessage(), e);
            // 不重新抛出异常，避免毒消息导致无限重投
            // 生产环境应配合死信队列（DLQ）+ 重试上限，将超过重试次数的消息转入 DLQ
        }
    }
}
