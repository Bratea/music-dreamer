package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.UserInfoResponse;
import com.musicdreamer.entity.User;
import com.musicdreamer.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户信息查询与校验")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public CommonResult<UserInfoResponse> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return CommonResult.error("用户不存在");
        }
        // 返回不含密码哈希的 DTO，避免敏感信息泄露
        return CommonResult.success(toUserInfoResponse(user));
    }

    @PutMapping
    public CommonResult<Boolean> update(@RequestBody User user) {
        // 防止 mass-assignment：仅允许更新安全字段，关键字段由服务端控制
        User existing = userService.getById(user.getUserId());
        if (existing == null) {
            return CommonResult.error("用户不存在");
        }
        // 仅覆盖允许用户自行修改的字段
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getAvatar() != null) existing.setAvatar(user.getAvatar());
        if (user.getGender() != null) existing.setGender(user.getGender());
        if (user.getBirthday() != null) existing.setBirthday(user.getBirthday());
        if (user.getSignature() != null) existing.setSignature(user.getSignature());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        return CommonResult.success(userService.updateById(existing));
    }

    /** 将 User 实体映射为不含密码的 UserInfoResponse */
    private UserInfoResponse toUserInfoResponse(User user) {
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
        return resp;
    }

    @GetMapping("/check/username")
    @Operation(summary = "检查用户名是否存在")
    public CommonResult<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsUsername(username);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return CommonResult.success(result);
    }

    @GetMapping("/check/email")
    @Operation(summary = "检查邮箱是否已注册")
    public CommonResult<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsEmail(email);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return CommonResult.success(result);
    }
}
