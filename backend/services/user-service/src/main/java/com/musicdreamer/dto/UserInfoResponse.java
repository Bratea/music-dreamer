package com.musicdreamer.dto;

import lombok.Data;

@Data
public class UserInfoResponse {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private String birthday;
    private String signature;
    private java.util.List<String> roles;
}
