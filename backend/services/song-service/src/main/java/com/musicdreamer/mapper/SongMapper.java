package com.musicdreamer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicdreamer.entity.Song;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SongMapper extends BaseMapper<Song> {

    /**
     * 聚合查询总播放量：SELECT IFNULL(SUM(play_count), 0) FROM song
     */
    @Select("SELECT IFNULL(SUM(play_count), 0) FROM song")
    Long sumPlayCount();
}
