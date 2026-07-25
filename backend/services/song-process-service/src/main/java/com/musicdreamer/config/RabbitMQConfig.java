package com.musicdreamer.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "music.exchange";

    // 歌曲事件队列
    public static final String QUEUE_SONG_PUBLISH = "queue.song.publish";
    public static final String QUEUE_SONG_DELETE  = "queue.song.delete";
    public static final String QUEUE_SONG_OFFLINE = "queue.song.offline";
    public static final String ROUTING_PUBLISH = "song.publish";
    public static final String ROUTING_DELETE  = "song.delete";
    public static final String ROUTING_OFFLINE = "song.offline";

    @Bean
    public TopicExchange musicExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue songPublishQueue() {
        return QueueBuilder.durable(QUEUE_SONG_PUBLISH).build();
    }
    @Bean
    public Queue songDeleteQueue() {
        return QueueBuilder.durable(QUEUE_SONG_DELETE).build();
    }
    @Bean
    public Queue songOfflineQueue() {
        return QueueBuilder.durable(QUEUE_SONG_OFFLINE).build();
    }

    @Bean
    public Binding bindPublish() {
        return BindingBuilder.bind(songPublishQueue()).to(musicExchange()).with(ROUTING_PUBLISH);
    }
    @Bean
    public Binding bindDelete() {
        return BindingBuilder.bind(songDeleteQueue()).to(musicExchange()).with(ROUTING_DELETE);
    }
    @Bean
    public Binding bindOffline() {
        return BindingBuilder.bind(songOfflineQueue()).to(musicExchange()).with(ROUTING_OFFLINE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setPrefetchCount(5);
        return factory;
    }
}
