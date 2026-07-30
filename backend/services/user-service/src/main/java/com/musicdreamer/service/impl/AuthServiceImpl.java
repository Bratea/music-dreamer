package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.dto.LoginRequest;
import com.musicdreamer.dto.LoginResponse;
import com.musicdreamer.dto.RegisterRequest;
import com.musicdreamer.dto.UserInfoResponse;
import com.musicdreamer.entity.User;
import com.musicdreamer.mapper.UserMapper;
import com.musicdreamer.service.AuthService;
import com.musicdreamer.service.RoleService;
import com.musicdreamer.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private final UserMapper userMapper;
    private final RoleService roleService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserMapper userMapper, RoleService roleService,
                           JwtUtils jwtUtils, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleService = roleService;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .or()
                .eq(User::getEmail, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("sub", String.valueOf(user.getUserId()));
        List<String> roles = roleService.getRoleCodesByUserId(user.getUserId());
        claims.put("roles", String.join(",", roles));

        String accessToken = jwtUtils.generateAccessToken(claims);
        String refreshToken = jwtUtils.generateRefreshToken(claims);

        LoginResponse response = new LoginResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setExpireIn(jwtUtils.getExpiration());
        response.setRoles(roles);
        return response;
    }

    @Override
    public UserInfoResponse register(RegisterRequest request) {
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()))) {
            throw new RuntimeException("用户名已存在");
        }
        if (userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()))) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setStatus(1);
        userMapper.insert(user);

        return toUserInfo(user);
    }

    @Override
    public UserInfoResponse getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return toUserInfo(user);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        Claims claims = jwtUtils.parseToken(refreshToken);
        String userId = claims.getSubject();
        if (userId == null) {
            throw new RuntimeException("无效的刷新令牌");
        }
        User user = getById(Long.valueOf(userId));
        if (user == null || user.getStatus() != 1) {
            throw new RuntimeException("用户不存在或已被禁用");
        }
        Map<String, Object> newClaims = new HashMap<>();
        newClaims.put("userId", user.getUserId());
        newClaims.put("username", user.getUsername());
        newClaims.put("sub", String.valueOf(user.getUserId()));
        List<String> roles = roleService.getRoleCodesByUserId(user.getUserId());
        newClaims.put("roles", String.join(",", roles));

        String newAccessToken = jwtUtils.generateAccessToken(newClaims);
        String newRefreshToken = jwtUtils.generateRefreshToken(newClaims);

        LoginResponse response = new LoginResponse();
        response.setToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setExpireIn(jwtUtils.getExpiration());
        response.setRoles(roles);
        return response;
    }

    private UserInfoResponse toUserInfo(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setUserId(user.getUserId());
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setAvatar(user.getAvatar());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setGender(user.getGender());
        resp.setBirthday(user.getBirthday());
        resp.setSignature(user.getSignature());
        resp.setRoles(roleService.getRoleCodesByUserId(user.getUserId()));
        return resp;
    }
}
