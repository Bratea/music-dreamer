package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Role;
import com.musicdreamer.entity.UserRole;
import com.musicdreamer.mapper.RoleMapper;
import com.musicdreamer.mapper.UserRoleMapper;
import com.musicdreamer.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final UserRoleMapper userRoleMapper;

    public RoleServiceImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        return userRoles.stream()
                .map(ur -> getById(ur.getRoleId()))
                .filter(r -> r != null)
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    @Override
    public void assignRole(Long userId, Long roleId) {
        // 先删除旧的，再插入新的（避免重复）
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId));
        UserRole ur = new UserRole();
        ur.setUserId(userId);
        ur.setRoleId(roleId);
        ur.setCreateTime(java.time.LocalDateTime.now());
        userRoleMapper.insert(ur);
    }

    @Override
    public void revokeRole(Long userId, Long roleId) {
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId));
    }
}
