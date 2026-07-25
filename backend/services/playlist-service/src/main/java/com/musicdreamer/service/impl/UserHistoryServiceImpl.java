package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.UserHistory;
import com.musicdreamer.mapper.UserHistoryMapper;
import com.musicdreamer.service.UserHistoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserHistoryServiceImpl
        extends ServiceImpl<UserHistoryMapper, UserHistory>
        implements UserHistoryService {

    @Override
    public void recordHistory(Long userId, Long songId, Integer playDuration) {
        UserHistory h = new UserHistory();
        h.setUserId(userId);
        h.setSongId(songId);
        h.setPlayDuration(playDuration == null ? 0 : playDuration);
        h.setPlayTime(LocalDateTime.now());
        save(h);
    }

    @Override
    public List<UserHistory> getRecentHistory(Long userId, int limit) {
        return lambdaQuery()
                .eq(UserHistory::getUserId, userId)
                .orderByDesc(UserHistory::getPlayTime)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public void clearHistory(Long userId) {
        lambdaUpdate()
                .eq(UserHistory::getUserId, userId)
                .remove();
    }
}
