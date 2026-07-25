package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.UserCollect;
import java.util.List;

public interface UserCollectService extends IService<UserCollect> {
    /**
     * 收藏/取消收藏（含分布式锁，防止并发重复操作）
     */
    boolean toggleCollect(Long userId, Long targetId, Integer targetType);

    /**
     * 是否已收藏
     */
    boolean isCollected(Long userId, Long targetId, Integer targetType);

    /**
     * 用户收藏列表
     */
    List<UserCollect> getUserCollects(Long userId, Integer targetType);
}
