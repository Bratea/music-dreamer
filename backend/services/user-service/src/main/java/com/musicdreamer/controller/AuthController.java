package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.LoginRequest;
import com.musicdreamer.dto.LoginResponse;
import com.musicdreamer.dto.RegisterRequest;
import com.musicdreamer.dto.UserInfoResponse;
import com.musicdreamer.service.AuthService;
import com.musicdreamer.util.JwtUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册、获取用户信息")
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "支持用户名或邮箱登录")
    public CommonResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            return CommonResult.success(authService.login(request));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册，自动BCrypt加密密码")
    public CommonResult<UserInfoResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return CommonResult.success(authService.register(request));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用 refreshToken 换取新的 accessToken")
    public CommonResult<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        try {
            String refreshToken = body.get("refreshToken");
            if (refreshToken == null || refreshToken.isBlank()) {
                return CommonResult.error(400, "refreshToken 不能为空");
            }
            return CommonResult.success(authService.refreshToken(refreshToken));
        } catch (RuntimeException e) {
            return CommonResult.error(401, e.getMessage());
        }
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取当前用户信息")
    public CommonResult<UserInfoResponse> getUserInfo(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null) {
            return CommonResult.error(401, "未登录或token缺失");
        }
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                return CommonResult.error(401, "token无效");
            }
            return CommonResult.success(authService.getUserInfo(userId));
        } catch (Exception e) {
            return CommonResult.error(401, "token解析失败");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
