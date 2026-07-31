package com.musicdreamer.service;

import com.musicdreamer.config.RabbitMQConfig;
import com.musicdreamer.dto.SongDTO;
import com.musicdreamer.entity.SongPublishEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SongEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:music.exchange}")
    private String exchange;

    // 默认 routing key 必须与 RabbitMQConfig 中的 binding key (song.publish) 一致，否则消息被交换器静默丢弃
    @Value("${rabbitmq.routing.publish:song.publish}")
    private String routingKey;

    public SongEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSongCreated(SongDTO song) {
        SongPublishEvent event = SongPublishEvent.of(song);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
