package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("song_audit")
public class SongAudit {
    @TableId(type = IdType.AUTO)
    private Long auditId;
    private Long songId;
    private Integer status;       // 0:待审核 1:通过 2:下架/拒绝
    private String rejectReason;
    private LocalDateTime auditTime;
    private Long auditorId;
}
