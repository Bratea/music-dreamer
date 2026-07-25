package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_history")
public class UserHistory {
    @TableId(type = IdType.AUTO)
    private Long historyId;
    private Long userId;
    private Long songId;
    private Integer playDuration;
    private LocalDateTime playTime;
}
