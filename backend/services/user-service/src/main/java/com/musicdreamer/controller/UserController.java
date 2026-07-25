package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
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
    public CommonResult<User> getById(@PathVariable Long id) {
        return CommonResult.success(userService.getById(id));
    }

    @PutMapping
    public CommonResult<Boolean> update(@RequestBody User user) {
        return CommonResult.success(userService.updateById(user));
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
