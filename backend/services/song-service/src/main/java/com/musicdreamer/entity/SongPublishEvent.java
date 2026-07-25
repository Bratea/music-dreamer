package com.musicdreamer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 歌曲发布事件（song-service 本地定义，用于 RabbitMQ 消息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongPublishEvent implements Serializable {

    private Long songId;
    private String name;
    private Long singerId;
    private String singerName;
    private String genre;
    private Integer action; // 1=发布 2=删除 3=下架

    public static SongPublishEvent of(Song song) {
        SongPublishEvent event = new SongPublishEvent();
        event.setSongId(song.getSongId());
        event.setName(song.getName());
        event.setSingerId(song.getSingerId());
        event.setGenre(song.getGenre());
        event.setAction(1);
        return event;
    }
}
