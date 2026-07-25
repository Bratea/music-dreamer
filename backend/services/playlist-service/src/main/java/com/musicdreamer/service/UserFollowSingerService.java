package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.UserFollowSinger;
import java.util.List;

public interface UserFollowSingerService extends IService<UserFollowSinger> {

    /**
     * 关注/取消关注（toggle）
     * @return true=已关注, false=已取消
     */
    boolean toggleFollow(Long userId, Long singerId);

    /**
     * 是否已关注某歌手
     */
    boolean isFollowing(Long userId, Long singerId);

    /**
     * 我关注的歌手 ID 列表
     */
    List<Long> getFollowingIds(Long userId);

    /**
     * 我关注的全部记录
     */
    List<UserFollowSinger> getFollowing(Long userId);
}
