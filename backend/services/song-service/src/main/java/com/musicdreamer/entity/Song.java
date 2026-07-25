package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("song")
public class Song {
    @TableId(type = IdType.AUTO)
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
