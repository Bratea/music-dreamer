package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户播放历史（song-service 本地副本，用于推荐计算）
 */
@Data
@TableName("user_history")
public class UserHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long songId;

    private LocalDateTime playTime;
}
