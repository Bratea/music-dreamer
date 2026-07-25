package com.musicdreamer.dto;

import lombok.Data;

/**
 * 歌曲 DTO（song-process-service 本地，用于接收消息）
 */
@Data
public class SongDTO {
    private Long songId;
    private String name;
    private Long singerId;
    private String singerName;
    private String genre;
    private String language;
    private Integer status;
}
