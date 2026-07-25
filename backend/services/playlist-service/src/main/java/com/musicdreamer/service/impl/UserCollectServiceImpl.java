package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.UserCollect;
import com.musicdreamer.mapper.UserCollectMapper;
import com.musicdreamer.service.UserCollectService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserCollectServiceImpl
        extends ServiceImpl<UserCollectMapper, UserCollect>
        implements UserCollectService {

    @Override
    public boolean toggleCollect(Long userId, Long targetId, Integer targetType) {
        UserCollect existing = lambdaQuery()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getTargetId, targetId)
                .eq(UserCollect::getTargetType, targetType)
                .one();
        if (existing != null) {
            removeById(existing.getCollectId());
            return false;
        }
        UserCollect c = new UserCollect();
        c.setUserId(userId);
        c.setTargetId(targetId);
        c.setTargetType(targetType);
        c.setCreateTime(LocalDateTime.now());
        save(c);
        return true;
    }

    @Override
    public boolean isCollected(Long userId, Long targetId, Integer targetType) {
        return lambdaQuery()
                .eq(UserCollect::getUserId, userId)
                .eq(UserCollect::getTargetId, targetId)
                .eq(UserCollect::getTargetType, targetType)
                .exists();
    }

    @Override
    public List<UserCollect> getUserCollects(Long userId, Integer targetType) {
        LambdaQueryWrapper<UserCollect> qw = new LambdaQueryWrapper<UserCollect>()
                .eq(UserCollect::getUserId, userId)
                .orderByDesc(UserCollect::getCreateTime);
        if (targetType != null) qw.eq(UserCollect::getTargetType, targetType);
        return list(qw);
    }
}
