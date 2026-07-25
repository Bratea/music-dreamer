package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsUsername(String username);
    boolean existsEmail(String email);
}
