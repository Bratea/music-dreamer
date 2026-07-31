package com.musicdreamer.service;

import com.musicdreamer.dto.SongVO;
import com.musicdreamer.entity.Song;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock com.musicdreamer.mapper.SongMapper songMapper;
    @Mock com.musicdreamer.mapper.SingerMapper singerMapper;
    @Mock com.musicdreamer.mapper.UserHistoryMapper historyMapper;
    @Mock org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;
    @Mock org.springframework.data.redis.core.ValueOperations<String, Object> valueOps;

    @InjectMocks RecommendServiceImpl recommendService;

    @Test
    void getHotSongs_cacheMiss_loadsFromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("recommend:hot:songs")).thenReturn(null);

        var songs = List.of(new Song(){{setSongId(1L); setName("Hot1"); setPlayCount(9999);}});
        when(songMapper.selectList(any())).thenReturn(songs);
        // toSongVOs 会调用 singerMapper 补全 singerName
        when(singerMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> result = recommendService.getHotSongs(1, 10);
        assertThat(result.get("total")).isEqualTo(1);
        assertThat(result.get("page")).isEqualTo(1);
        // 缓存的是 SongVO 列表（生产代码 toSongVOs 转换后的类型），不是 Song
        verify(valueOps).set(eq("recommend:hot:songs"), argThat(obj ->
                obj instanceof List<?> && !((List<?>) obj).isEmpty() &&
                        ((List<?>) obj).get(0) instanceof SongVO
        ), any(), any());
    }

    @Test
    void getHotSongs_cacheHit_returnsCached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        // 缓存命中时，存入的是 SongVO 类型
        var cached = List.of(new SongVO(){{setSongId(2L); setName("Cached"); setPlayCount(5000);}});
        when(valueOps.get("recommend:hot:songs")).thenReturn(cached);

        Map<String, Object> result = recommendService.getHotSongs(1, 10);
        assertThat(result.get("total")).isEqualTo(1);
        verify(songMapper, never()).selectList(any());
    }
}
