package com.musicdreamer.service;

import com.musicdreamer.dto.LoginRequest;
import com.musicdreamer.dto.LoginResponse;
import com.musicdreamer.dto.RegisterRequest;
import com.musicdreamer.dto.UserInfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserService userService;
    @Mock com.musicdreamer.mapper.UserMapper userMapper;
    @Mock com.musicdreamer.util.JwtUtils jwtUtils;

    @InjectMocks com.musicdreamer.service.impl.AuthServiceImpl authService;

    @Test
    void login_success() {
        // Arrange
        var user = new com.musicdreamer.entity.User();
        user.setUserId(1L);
        user.setUsername("test");
        user.setNickname("Tester");
        user.setStatus(1);
        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtUtils.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");
        when(jwtUtils.getExpiration()).thenReturn(86400000L);

        var req = new LoginRequest();
        req.setUsername("test");
        req.setPassword("password");

        // Act
        LoginResponse resp = authService.login(req);

        // Assert
        assertThat(resp).isNotNull();
        assertThat(resp.getToken()).isEqualTo("access-token");
        assertThat(resp.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(resp.getUserId()).isEqualTo(1L);
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    void login_fail_wrongPassword() {
        var user = new com.musicdreamer.entity.User();
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secret"));
        when(userMapper.selectOne(any())).thenReturn(user);

        var req = new LoginRequest();
        req.setUsername("test");
        req.setPassword("wrong-password");

        assertThrows(RuntimeException.class, () -> authService.login(req));
    }

    @Test
    void register_success() {
        when(userMapper.exists(any())).thenReturn(false);
        when(userMapper.insert(any())).thenReturn(1);

        var req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("pass123");
        req.setEmail("new@test.com");

        UserInfoResponse resp = authService.register(req);
        assertThat(resp).isNotNull();
        assertThat(resp.getUsername()).isEqualTo("newuser");
        verify(userMapper, times(1)).insert(any());
    }

    @Test
    void register_duplicateUsername() {
        when(userMapper.exists(any())).thenReturn(true);
        var req = new RegisterRequest();
        req.setUsername("existing");
        req.setPassword("pass");
        req.setEmail("a@b.com");
        assertThrows(RuntimeException.class, () -> authService.register(req));
    }
}
