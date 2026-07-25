package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.UserCollect;

public interface SongCollectService extends IService<UserCollect> {

    /**
     * 收藏 / 取消收藏歌曲（toggle）
     * @return true=已收藏, false=已取消
     */
    boolean toggleCollect(Long userId, Long songId);

    /**
     * 是否已收藏该歌曲
     */
    boolean isCollected(Long userId, Long songId);
}
