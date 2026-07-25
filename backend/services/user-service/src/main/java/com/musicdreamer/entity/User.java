package com.musicdreamer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private String nickname;
    private Integer gender;
    private String birthday;
    private String signature;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
