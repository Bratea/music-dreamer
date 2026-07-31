package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.entity.Song;
import com.musicdreamer.service.SongService;
import com.musicdreamer.service.RecommendService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/song")
@Tag(name = "歌曲管理", description = "歌曲CRUD、播放量统计、收藏")
public class SongController {

    private final SongService songService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RecommendService recommendService;

    public SongController(SongService songService, RedisTemplate<String, Object> redisTemplate,
                          RecommendService recommendService) {
        this.songService = songService;
        this.redisTemplate = redisTemplate;
        this.recommendService = recommendService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询歌曲详情")
    public CommonResult<Song> getById(@PathVariable("id") Long id) {
        return CommonResult.success(songService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增歌曲（管理员/歌手）")
    public CommonResult<Boolean> create(@Valid @RequestBody Song song) {
        return CommonResult.success(songService.save(song));
    }

    @PutMapping
    @Operation(summary = "更新歌曲信息")
    public CommonResult<Boolean> update(@Valid @RequestBody Song song) {
        // 防止 updateById 将请求中未传的字段覆写为 null（数据丢失）
        Song existing = songService.getById(song.getSongId());
        if (existing == null) {
            return CommonResult.error("歌曲不存在");
        }
        // 仅覆盖请求中显式提供的字段，其余保留原值
        mergeSongFields(song, existing);
        return CommonResult.success(songService.updateById(existing));
    }

    /** 将 source 中非 null 字段复制到 target，避免全字段覆写导致数据丢失 */
    private void mergeSongFields(Song source, Song target) {
        if (source.getName() != null) target.setName(source.getName());
        if (source.getSingerId() != null) target.setSingerId(source.getSingerId());
        if (source.getAlbumId() != null) target.setAlbumId(source.getAlbumId());
        if (source.getDuration() != null) target.setDuration(source.getDuration());
        if (source.getUrl() != null) target.setUrl(source.getUrl());
        if (source.getCover() != null) target.setCover(source.getCover());
        if (source.getLyrics() != null) target.setLyrics(source.getLyrics());
        if (source.getDescription() != null) target.setDescription(source.getDescription());
        if (source.getGenre() != null) target.setGenre(source.getGenre());
        if (source.getLanguage() != null) target.setLanguage(source.getLanguage());
        if (source.getReleaseDate() != null) target.setReleaseDate(source.getReleaseDate());
        if (source.getStatus() != null) target.setStatus(source.getStatus());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌曲")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        return CommonResult.success(songService.removeById(id));
    }

    // ── 播放量统计 ──

    @PostMapping("/{id}/play")
    @Operation(summary = "播放歌曲", description = "播放量+1（Redis 计数，定时任务异步持久化到DB）")
    public CommonResult<Map<String, Object>> play(@PathVariable("id") Long id) {
        String key = "play:count:" + id;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) redisTemplate.expire(key, 6, java.util.concurrent.TimeUnit.HOURS);
        // 仅递增 Redis 计数器；DB 持久化由定时任务统一 flush，避免每次请求写 DB
        // 返回 DB 真实累计播放量 + 当前窗口增量
        Long totalPlayCount = songService.getById(id) != null ? songService.getById(id).getPlayCount() : 0L;
        return CommonResult.success(Map.of("songId", id, "playCount", totalPlayCount + count));
    }

    @GetMapping("/hot")
    @Operation(summary = "热门歌曲", description = "按播放量排序 TopN")
    public CommonResult<Map<String, Object>> getHotSongs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return CommonResult.success(recommendService.getHotSongs(page, size));
    }

    @GetMapping("/{id}/similar")
    @Operation(summary = "相似歌曲推荐", description = "同流派高播放量歌曲")
    public CommonResult<Map<String, Object>> getSimilarSongs(@PathVariable("id") Long id,
                                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        return CommonResult.success(recommendService.getSimilarSongs(id, size));
    }

    @GetMapping("/new")
    @Operation(summary = "最新上架", description = "按发行日期倒序")
    public CommonResult<Map<String, Object>> getNewSongs(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return CommonResult.success(recommendService.getNewSongs(page, size));
    }

    // ── 跨服务调用接口（供 user-service Feign 使用）──

    @GetMapping("/count")
    @Operation(summary = "统计歌曲总数", description = "供管理后台统计使用")
    public CommonResult<Long> count() {
        return CommonResult.success(songService.count());
    }

    @GetMapping("/list/all")
    @Operation(summary = "查询所有歌曲", description = "供管理后台统计使用")
    public CommonResult<java.util.List<Song>> listAll() {
        return CommonResult.success(songService.list());
    }
}
