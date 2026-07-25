package com.musicdreamer.dto;

import com.musicdreamer.entity.SingerApply;
import com.musicdreamer.entity.User;
import lombok.Data;

@Data
public class SingerApplyVO extends SingerApply {
    private String username;
    private String nickname;
    private String email;
}
