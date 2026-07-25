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
        return CommonResult.success(songService.updateById(song));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌曲")
    public CommonResult<Boolean> delete(@PathVariable("id") Long id) {
        return CommonResult.success(songService.removeById(id));
    }

    // ── 播放量统计 ──

    @PostMapping("/{id}/play")
    @Operation(summary = "播放歌曲", description = "播放量+1，异步持久化到DB")
    public CommonResult<Map<String, Object>> play(@PathVariable("id") Long id) {
        String key = "play:count:" + id;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) redisTemplate.expire(key, 6, java.util.concurrent.TimeUnit.HOURS);
        songService.lambdaUpdate().eq(Song::getSongId, id).setSql("play_count = play_count + 1").update();
        return CommonResult.success(Map.of("songId", id, "playCount", count));
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
