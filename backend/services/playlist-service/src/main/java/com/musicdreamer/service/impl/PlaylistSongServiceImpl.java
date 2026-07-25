package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.PlaylistSong;
import com.musicdreamer.mapper.PlaylistSongMapper;
import com.musicdreamer.service.PlaylistSongService;
import org.springframework.stereotype.Service;

@Service
public class PlaylistSongServiceImpl extends ServiceImpl<PlaylistSongMapper, PlaylistSong> implements PlaylistSongService {
}
