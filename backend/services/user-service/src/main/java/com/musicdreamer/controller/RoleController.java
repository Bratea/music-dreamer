package com.musicdreamer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicdreamer.common.CommonResult;
import com.musicdreamer.entity.Role;
import com.musicdreamer.entity.User;
import com.musicdreamer.entity.UserRole;
import com.musicdreamer.service.RoleService;
import com.musicdreamer.service.UserRoleService;
import com.musicdreamer.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/role")
@Tag(name = "角色管理", description = "[管理员] 角色分配与查询")
public class RoleController {

    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final UserService userService;

    public RoleController(RoleService roleService, UserRoleService userRoleService, UserService userService) {
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.userService = userService;
    }

    @GetMapping("/list")
    @Operation(summary = "角色列表")
    public CommonResult<List<Role>> list() {
        return CommonResult.success(roleService.list());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户角色列表")
    public CommonResult<List<String>> getUserRoles(@PathVariable Long userId) {
        return CommonResult.success(roleService.getRoleCodesByUserId(userId));
    }

    @PostMapping("/assign")
    @Operation(summary = "给用户分配角色")
    public CommonResult<Void> assignRole(@RequestParam Long userId, @RequestParam Long roleId) {
        userRoleService.grantRole(userId, roleId);
        return CommonResult.success(null);
    }

    @DeleteMapping("/revoke")
    @Operation(summary = "撤销用户角色")
    public CommonResult<Void> revokeRole(@RequestParam Long userId, @RequestParam Long roleId) {
        userRoleService.revokeRole(userId, roleId);
        return CommonResult.success(null);
    }

    @GetMapping("/user-detail/{userId}")
    @Operation(summary = "获取用户详情+角色")
    public CommonResult<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) return CommonResult.error(404, "用户不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("status", user.getStatus());
        result.put("roles", roleService.getRoleCodesByUserId(userId));
        result.put("createTime", user.getCreateTime());
        return CommonResult.success(result);
    }
}
