package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Playlist;
import com.musicdreamer.entity.PlaylistSong;
import com.musicdreamer.mapper.PlaylistMapper;
import com.musicdreamer.mapper.PlaylistSongMapper;
import com.musicdreamer.service.PlaylistService;
import com.musicdreamer.service.PlaylistSongService;
import org.springframework.stereotype.Service;

@Service
public class PlaylistServiceImpl extends ServiceImpl<PlaylistMapper, Playlist> implements PlaylistService {
}
