package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.UserCollect;
import com.musicdreamer.mapper.UserCollectMapper;
import com.musicdreamer.service.SongCollectService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SongCollectServiceImpl
        extends ServiceImpl<UserCollectMapper, UserCollect>
        implements SongCollectService {

    private static final int TARGET_TYPE_SONG = 1;

    @Override
    public boolean toggleCollect(Long userId, Long songId) {
        UserCollect existing = lambdaQuery()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getTargetId, songId)
                .eq(UserCollect::getTargetType, TARGET_TYPE_SONG)
                .one();
        if (existing != null) {
            removeById(existing.getCollectId());
            return false;
        }
        UserCollect c = new UserCollect();
        c.setUserId(userId);
        c.setTargetId(songId);
        c.setTargetType(TARGET_TYPE_SONG);
        c.setCreateTime(LocalDateTime.now());
        save(c);
        return true;
    }

    @Override
    public boolean isCollected(Long userId, Long songId) {
        return lambdaQuery()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getTargetId, songId)
                .eq(UserCollect::getTargetType, TARGET_TYPE_SONG)
                .exists();
    }
}
