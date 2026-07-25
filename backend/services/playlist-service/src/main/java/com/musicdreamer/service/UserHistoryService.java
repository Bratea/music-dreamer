package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.UserHistory;

import java.util.List;

public interface UserHistoryService extends IService<UserHistory> {
    /**
     * 记录播放历史
     */
    void recordHistory(Long userId, Long songId, Integer playDuration);

    /**
     * 获取用户最近播放历史
     */
    List<UserHistory> getRecentHistory(Long userId, int limit);

    /**
     * 清空用户播放历史
     */
    void clearHistory(Long userId);
}
