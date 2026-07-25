package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_collect")
public class UserCollect {
    @TableId(type = IdType.AUTO)
    private Long collectId;
    private Long userId;
    private Long targetId;
    private Integer targetType;  // 1:歌曲  2:歌单
    private LocalDateTime createTime;
}
