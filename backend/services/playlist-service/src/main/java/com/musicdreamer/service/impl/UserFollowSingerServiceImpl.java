package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.UserFollowSinger;
import com.musicdreamer.mapper.UserFollowSingerMapper;
import com.musicdreamer.service.UserFollowSingerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserFollowSingerServiceImpl
        extends ServiceImpl<UserFollowSingerMapper, UserFollowSinger>
        implements UserFollowSingerService {

    @Override
    public boolean toggleFollow(Long userId, Long singerId) {
        UserFollowSinger existing = lambdaQuery()
                .eq(UserFollowSinger::getUserId, userId)
                .eq(UserFollowSinger::getSingerId, singerId)
                .one();
        if (existing != null) {
            removeById(existing.getId());
            return false;
        }
        UserFollowSinger f = new UserFollowSinger();
        f.setUserId(userId);
        f.setSingerId(singerId);
        f.setCreateTime(LocalDateTime.now());
        save(f);
        return true;
    }

    @Override
    public boolean isFollowing(Long userId, Long singerId) {
        return lambdaQuery()
                .eq(UserFollowSinger::getUserId, userId)
                .eq(UserFollowSinger::getSingerId, singerId)
                .exists();
    }

    @Override
    public List<Long> getFollowingIds(Long userId) {
        return lambdaQuery()
                .eq(UserFollowSinger::getUserId, userId)
                .orderByDesc(UserFollowSinger::getCreateTime)
                .list()
                .stream()
                .map(UserFollowSinger::getSingerId)
                .toList();
    }

    @Override
    public List<UserFollowSinger> getFollowing(Long userId) {
        return lambdaQuery()
                .eq(UserFollowSinger::getUserId, userId)
                .orderByDesc(UserFollowSinger::getCreateTime)
                .list();
    }
}
