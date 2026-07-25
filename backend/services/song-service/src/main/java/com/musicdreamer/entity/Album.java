package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("album")
public class Album {
    @TableId(type = IdType.AUTO)
    private Long albumId;
    private String name;
    private Long singerId;
    private String cover;
    private String description;
    private String releaseDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
