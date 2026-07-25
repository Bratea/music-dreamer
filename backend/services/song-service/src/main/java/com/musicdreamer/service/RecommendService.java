package com.musicdreamer.service;

import java.util.Map;

public interface RecommendService {
    /**
     * 热门推荐 (基于播放量 TopN)
     */
    Map<String, Object> getHotSongs(int page, int size);

    /**
     * 个性化推荐 (基于用户历史听歌风格)
     */
    Map<String, Object> getPersonalRecommend(Long userId, int size);

    /**
     * 相似歌曲推荐 (基于标签匹配)
     */
    Map<String, Object> getSimilarSongs(Long songId, int size);

    /**
     * 最新上架 (按发行日期倒序)
     */
    Map<String, Object> getNewSongs(int page, int size);

    /**
     * 每日推荐 (每日更新一次的热门歌单)
     */
    Map<String, Object> getDailyRecommend(Long userId, int size);
}
