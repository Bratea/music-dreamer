package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("singer_apply")
public class SingerApply {
    @TableId(type = IdType.AUTO)
    private Long applyId;
    private Long userId;
    private String realName;
    private String idCard;
    private String avatar;
    private String intro;
    private Integer gender;
    private String country;
    private String birthday;
    private Integer status;        // 0待审核 1通过 2拒绝
    private String rejectReason;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
}
