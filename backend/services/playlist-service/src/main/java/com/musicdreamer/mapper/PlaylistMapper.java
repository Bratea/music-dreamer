package com.musicdreamer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicdreamer.entity.Playlist;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlaylistMapper extends BaseMapper<Playlist> {
}
