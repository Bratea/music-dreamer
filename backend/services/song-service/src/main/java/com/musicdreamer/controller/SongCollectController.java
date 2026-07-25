package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.service.SongCollectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 歌曲收藏接口（网关将 /api/song/** 原样路由到 song-service，不剥离前缀）
 */
@RestController
@RequestMapping("/api/song/collect")
@Tag(name = "歌曲收藏", description = "歌曲收藏/取消收藏/是否收藏")
public class SongCollectController {

    private final SongCollectService songCollectService;

    public SongCollectController(SongCollectService songCollectService) {
        this.songCollectService = songCollectService;
    }

    @PostMapping
    @Operation(summary = "收藏歌曲（toggle）", description = "Body: {songId}，已收藏则取消")
    public CommonResult<Map<String, Object>> collect(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Long> body) {
        Long songId = body.get("songId");
        boolean collected = songCollectService.toggleCollect(userId, songId);
        return CommonResult.success(Map.of("songId", songId, "collected", collected));
    }

    @DeleteMapping("/{songId}")
    @Operation(summary = "取消收藏歌曲")
    public CommonResult<Map<String, Object>> uncollect(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long songId) {
        songCollectService.toggleCollect(userId, songId);
        return CommonResult.success(Map.of("songId", songId, "collected", false));
    }

    @GetMapping("/{songId}")
    @Operation(summary = "是否已收藏该歌曲")
    public CommonResult<Map<String, Object>> isCollected(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long songId) {
        boolean collected = songCollectService.isCollected(userId, songId);
        return CommonResult.success(Map.of("songId", songId, "collected", collected));
    }
}
