package com.musicdreamer.service.event;

import com.musicdreamer.entity.Song;
import com.musicdreamer.entity.SongPublishEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 歌曲事件发布者（song-service 本地实现）
 * 通过 RabbitMQ 发送歌曲发布事件
 */
@Service
public class SongEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:music.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.publish:queue.song.publish}")
    private String routingKey;

    public SongEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSongCreated(Song song) {
        SongPublishEvent event = SongPublishEvent.of(song);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
