package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.UserRole;
import com.musicdreamer.mapper.UserRoleMapper;
import com.musicdreamer.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Override
    public void grantRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId);
        if (!exists(qw)) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            ur.setCreateTime(java.time.LocalDateTime.now());
            save(ur);
        }
    }

    @Override
    public void revokeRole(Long userId, Long roleId) {
        lambdaUpdate().eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
                .remove();
    }

    @Override
    public boolean hasRole(Long userId, Long roleId) {
        LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId);
        return exists(qw);
    }
}
