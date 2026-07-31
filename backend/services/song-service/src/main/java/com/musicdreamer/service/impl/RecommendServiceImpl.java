package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicdreamer.dto.SongVO;
import com.musicdreamer.entity.Singer;
import com.musicdreamer.entity.Song;
import com.musicdreamer.entity.UserHistory;
import com.musicdreamer.mapper.SingerMapper;
import com.musicdreamer.mapper.SongMapper;
import com.musicdreamer.mapper.UserHistoryMapper;
import com.musicdreamer.service.RecommendService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    private final SongMapper songMapper;
    private final SingerMapper singerMapper;
    private final UserHistoryMapper historyMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_SONGS_KEY = "recommend:hot:songs";

    public RecommendServiceImpl(SongMapper songMapper, SingerMapper singerMapper,
                                UserHistoryMapper historyMapper,
                                RedisTemplate<String, Object> redisTemplate) {
        this.songMapper = songMapper;
        this.singerMapper = singerMapper;
        this.historyMapper = historyMapper;
        this.redisTemplate = redisTemplate;
    }

    /** 将 Song 列表转为 SongVO 并批量补全 singerName */
    private List<SongVO> toSongVOs(List<Song> songs) {
        if (songs.isEmpty()) return List.of();
        Set<Long> singerIds = songs.stream().map(Song::getSingerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> singerNameMap = new HashMap<>();
        if (!singerIds.isEmpty()) {
            var singers = singerMapper.selectList(
                    new LambdaQueryWrapper<Singer>().in(Singer::getSingerId, singerIds));
            for (Singer s : singers) singerNameMap.put(s.getSingerId(), s.getName());
        }
        return songs.stream().map(song -> {
            SongVO vo = new SongVO();
            vo.setSongId(song.getSongId());
            vo.setName(song.getName());
            vo.setSingerId(song.getSingerId());
            vo.setAlbumId(song.getAlbumId());
            vo.setDuration(song.getDuration());
            vo.setUrl(song.getUrl());
            vo.setCover(song.getCover());
            vo.setLyrics(song.getLyrics());
            vo.setDescription(song.getDescription());
            vo.setGenre(song.getGenre());
            vo.setLanguage(song.getLanguage());
            vo.setReleaseDate(song.getReleaseDate());
            vo.setPlayCount(song.getPlayCount());
            vo.setLikeCount(song.getLikeCount());
            vo.setStatus(song.getStatus());
            vo.setCreateTime(song.getCreateTime());
            vo.setUpdateTime(song.getUpdateTime());
            vo.setSingerName(singerNameMap.getOrDefault(song.getSingerId(), ""));
            // albumName 暂不补全，需要时可通过 albumId 回查
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHotSongs(int page, int size) {
        List<SongVO> songs = null;
        try {
            @SuppressWarnings("unchecked")
            List<SongVO> cached = (List<SongVO>) redisTemplate.opsForValue().get(HOT_SONGS_KEY);
            songs = cached;
        } catch (Exception e) {
            // Redis unavailable, fallback to DB
        }
        if (songs == null) {
            List<Song> entities = songMapper.selectList(
                    new LambdaQueryWrapper<Song>()
                            .eq(Song::getStatus, 1)
                            .orderByDesc(Song::getPlayCount)
                            .last("LIMIT 100")
            );
            songs = toSongVOs(entities);
            try {
                redisTemplate.opsForValue().set(HOT_SONGS_KEY, songs, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                // Redis unavailable, ignore cache write
            }
        }
        // 防御非法页码，避免负数 index 导致 IndexOutOfBoundsException
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, songs.size());
        return Map.of("songs", from < to ? songs.subList(from, to) : List.of(),
                "total", songs.size(), "page", safePage, "size", safeSize);
    }

    @Override
    public Map<String, Object> getPersonalRecommend(Long userId, int size) {
        var histories = historyMapper.selectList(
                new LambdaQueryWrapper<UserHistory>()
                        .eq(UserHistory::getUserId, userId)
                        .orderByDesc(UserHistory::getPlayTime)
                        .last("LIMIT 200"));
        if (histories.isEmpty()) return getHotSongs(1, size);

        // 批量查询歌曲，避免 N+1
        List<Long> songIds = histories.stream().map(UserHistory::getSongId).distinct().toList();
        Map<Long, Song> songMap = songIds.isEmpty() ? Map.of()
                : songMapper.selectList(new LambdaQueryWrapper<Song>().in(Song::getSongId, songIds))
                        .stream().collect(Collectors.toMap(Song::getSongId, s -> s));

        Map<String, Long> genreCount = new HashMap<>();
        for (UserHistory h : histories) {
            Song s = songMap.get(h.getSongId());
            if (s != null && s.getGenre() != null)
                genreCount.merge(s.getGenre(), 1L, Long::sum);
        }

        var topGenres = genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
        if (topGenres.isEmpty()) return getHotSongs(1, size);

        Set<Long> heard = histories.stream().map(UserHistory::getSongId).collect(Collectors.toSet());
        List<Song> recs = new ArrayList<>();
        for (var e : topGenres) {
            var songs = songMapper.selectList(
                    new LambdaQueryWrapper<Song>()
                            .eq(Song::getGenre, e.getKey())
                            .eq(Song::getStatus, 1)
                            .notIn(heard.isEmpty(), Song::getSongId, heard)
                            .orderByDesc(Song::getPlayCount)
                            .last("LIMIT " + size));
            recs.addAll(songs);
            if (recs.size() >= size) break;
        }
        List<SongVO> vos = toSongVOs(recs).subList(0, Math.min(recs.size(), size));
        return Map.of("songs", vos, "strategy", "genre-preference");
    }

    @Override
    public Map<String, Object> getSimilarSongs(Long songId, int size) {
        Song target = songMapper.selectById(songId);
        if (target == null) return Map.of();
        var songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>()
                        .eq(Song::getGenre, target.getGenre())
                        .ne(Song::getSongId, songId)
                        .eq(Song::getStatus, 1)
                        .orderByDesc(Song::getPlayCount)
                        .last("LIMIT " + size));
        return Map.of("songs", toSongVOs(songs), "basedOn", songId);
    }

    @Override
    public Map<String, Object> getNewSongs(int page, int size) {
        // 限制 size 和 page 上限，防止 LIMIT 过大
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        var songs = songMapper.selectList(
                new LambdaQueryWrapper<Song>()
                        .eq(Song::getStatus, 1)
                        .orderByDesc(Song::getReleaseDate)
                        .last("LIMIT " + ((safePage - 1) * safeSize + safeSize))
        );
        int from = (safePage - 1) * safeSize;
        int to = Math.min(from + safeSize, songs.size());
        return Map.of("songs", from < to ? toSongVOs(songs).subList(from, to) : List.of(),
                "total", songs.size(), "page", safePage, "size", safeSize);
    }

    @Override
    public Map<String, Object> getDailyRecommend(Long userId, int size) {
        String key = "recommend:daily:" + userId;
        @SuppressWarnings("unchecked")
        List<SongVO> cached = (List<SongVO>) redisTemplate.opsForValue().get(key);
        if (cached != null) return Map.of("songs", cached, "source", "cache");
        Map<String, Object> base = getPersonalRecommend(userId, size);
        List<SongVO> songs = (List<SongVO>) base.get("songs");
        if (!songs.isEmpty()) redisTemplate.opsForValue().set(key, songs, 1, TimeUnit.DAYS);
        // getPersonalRecommend 返回的是不可变 Map.of()，需复制到可变 Map 再追加字段
        Map<String, Object> result = new HashMap<>(base);
        result.put("source", "generated");
        return result;
    }
}
