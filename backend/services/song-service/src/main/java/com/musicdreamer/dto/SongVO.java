package com.musicdreamer.dto;

import com.musicdreamer.entity.Song;
import lombok.Data;

@Data
public class SongVO extends Song {
    private String singerName;
    private String albumName;
}
