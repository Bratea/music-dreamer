package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Song;
import com.musicdreamer.mapper.SongMapper;
import com.musicdreamer.service.SongService;
import org.springframework.stereotype.Service;

@Service
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements SongService {

    @Override
    public Long sumPlayCount() {
        // 使用聚合 SQL：SELECT SUM(play_count) FROM song
        Long sum = baseMapper.selectOne(
                new LambdaQueryWrapper<Song>().select(Song::getPlayCount)
        ) == null ? 0L : null;
        // 通过 SQL 函数聚合
        return baseMapper.sumPlayCount();
    }
}
