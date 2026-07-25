package com.musicdreamer.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SongPublishEvent {
    private Long songId;
    private String name;
    private Long singerId;
    private String url;
    private String cover;
    private String genre;
    private String lyrics;
    private LocalDateTime publishTime;

    public static SongPublishEvent of(com.musicdreamer.dto.SongDTO song) {
        SongPublishEvent e = new SongPublishEvent();
        e.setSongId(song.getSongId());
        e.setName(song.getName());
        e.setSingerId(song.getSingerId());
        e.setGenre(song.getGenre());
        e.setPublishTime(LocalDateTime.now());
        return e;
    }
}
