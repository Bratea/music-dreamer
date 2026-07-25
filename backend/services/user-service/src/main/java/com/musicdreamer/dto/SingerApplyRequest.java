package com.musicdreamer.dto;

import com.musicdreamer.entity.SingerApply;
import lombok.Data;

@Data
public class SingerApplyRequest {
    private String realName;
    private String idCard;
    private String avatar;
    private String intro;
    private Integer gender;
    private String country;
    private String birthday;
}
