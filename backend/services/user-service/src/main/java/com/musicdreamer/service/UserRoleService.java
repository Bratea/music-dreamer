package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.UserRole;

public interface UserRoleService extends IService<UserRole> {
    void grantRole(Long userId, Long roleId);
    void revokeRole(Long userId, Long roleId);
    boolean hasRole(Long userId, Long roleId);
}
