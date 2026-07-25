package com.musicdreamer.service;

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

    @Value("${rabbitmq.routing.publish:queue.song.publish}")
    private String routingKey;

    public SongEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishSongCreated(SongDTO song) {
        SongPublishEvent event = SongPublishEvent.of(song);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
