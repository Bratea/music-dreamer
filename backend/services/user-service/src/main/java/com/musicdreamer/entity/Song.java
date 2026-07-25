package com.musicdreamer.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Song DTO (for Feign response deserialization from song-service)
 */
@Data
public class Song {
    private Long songId;
    private String name;
    private Long singerId;
    private Long albumId;
    private Integer duration;
    private String url;
    private String cover;
    private String lyrics;
    private String description;
    private String genre;
    private String language;
    private String releaseDate;
    private Integer playCount;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
