package com.musicdreamer.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 歌曲视图对象（不含继承，避免 JSON 多态反序列化问题）
 */
@Data
public class SongVO {
    private Long songId;
    private String name;
    private Long singerId;
    private String singerName;
    private Long albumId;
    private String albumName;
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
