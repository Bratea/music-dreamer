package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.Song;

public interface SongService extends IService<Song> {
    /**
     * 查询总播放量（聚合 SUM）
     */
    Long sumPlayCount();
}
