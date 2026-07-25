package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("singer")
public class Singer {
    @TableId(type = IdType.AUTO)
    private Long singerId;
    private String name;
    private String avatar;
    private String intro;
    private Integer gender;
    private String country;
    private String birthday;
    private Integer status;
    private Long userId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
