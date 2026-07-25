package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("playlist")
public class Playlist {
    @TableId(type = IdType.AUTO)
    private Long playlistId;
    private String name;
    private String cover;
    private String description;
    private Long userId;
    private Integer playCount;
    private Integer songCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
