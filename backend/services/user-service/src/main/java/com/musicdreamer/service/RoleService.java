package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<String> getRoleCodesByUserId(Long userId);
    void assignRole(Long userId, Long roleId);
    void revokeRole(Long userId, Long roleId);
}
