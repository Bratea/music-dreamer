package com.musicdreamer.service;

import com.musicdreamer.dto.LoginRequest;
import com.musicdreamer.dto.LoginResponse;
import com.musicdreamer.dto.RegisterRequest;
import com.musicdreamer.dto.UserInfoResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserInfoResponse register(RegisterRequest request);
    UserInfoResponse getUserInfo(Long userId);
}
