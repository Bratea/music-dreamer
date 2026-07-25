package com.musicdreamer.service;

import com.musicdreamer.entity.Song;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock com.musicdreamer.mapper.SongMapper songMapper;
    @InjectMocks com.musicdreamer.service.impl.SongServiceImpl songService;

    @Test
    void save_insertsSong() {
        Song song = new Song();
        song.setName("Test Song");
        when(songMapper.insert(song)).thenReturn(1);
        boolean ok = songService.save(song);
        assertThat(ok).isTrue();
        verify(songMapper).insert(song);
    }

    @Test
    void getById_returnsSong() {
        Song song = new Song();
        song.setSongId(1L);
        song.setName("Found");
        when(songMapper.selectById(1L)).thenReturn(song);
        assertThat(songService.getById(1L)).isEqualTo(song);
    }
}
