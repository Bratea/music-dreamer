package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.entity.Playlist;
import com.musicdreamer.service.PlaylistService;
import com.musicdreamer.service.PlaylistSongService;
import com.musicdreamer.entity.PlaylistSong;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
@Tag(name = "歌单管理", description = "歌单CRUD、歌曲添加/移除、收藏、推荐")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistSongService playlistSongService;

    public PlaylistController(PlaylistService playlistService, PlaylistSongService playlistSongService) {
        this.playlistService = playlistService;
        this.playlistSongService = playlistSongService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询歌单详情", description = "包含歌单信息和歌曲列表")
    public CommonResult<Map<String, Object>> getById(@PathVariable Long id) {
        Playlist playlist = playlistService.getById(id);
        if (playlist == null) return CommonResult.error(404, "歌单不存在");

        List<PlaylistSong> songs = playlistSongService.lambdaQuery()
                .eq(PlaylistSong::getPlaylistId, id)
                .orderByAsc(PlaylistSong::getSort)
                .list();

        Map<String, Object> result = new HashMap<>();
        result.put("playlist", playlist);
        result.put("songs", songs);
        result.put("songCount", songs.size());
        return CommonResult.success(result);
    }

    @PostMapping
    @Operation(summary = "创建歌单", description = "status=1 公开, 0 私密")
    public CommonResult<Boolean> create(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody Playlist playlist) {
        // 从认证身份获取 userId，不信任请求体中的 userId（防越权）
        playlist.setUserId(userId);
        playlist.setPlayCount(0);
        playlist.setSongCount(0);
        return CommonResult.success(playlistService.save(playlist));
    }

    @PutMapping
    @Operation(summary = "更新歌单信息")
    public CommonResult<Boolean> update(@RequestAttribute("userId") Long userId,
                                        @Valid @RequestBody Playlist playlist) {
        // 校验歌单归属，防止修改他人歌单
        Playlist existing = playlistService.getById(playlist.getPlaylistId());
        if (existing == null) return CommonResult.error("歌单不存在");
        if (!existing.getUserId().equals(userId)) {
            return CommonResult.error("无权操作该歌单");
        }
        return CommonResult.success(playlistService.updateById(playlist));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌单", description = "同时删除歌单歌曲关联")
    public CommonResult<Boolean> delete(@RequestAttribute("userId") Long userId,
                                        @PathVariable Long id) {
        Playlist existing = playlistService.getById(id);
        if (existing == null) return CommonResult.error("歌单不存在");
        if (!existing.getUserId().equals(userId)) {
            return CommonResult.error("无权操作该歌单");
        }
        boolean ok = playlistService.removeById(id);
        playlistSongService.lambdaUpdate().eq(PlaylistSong::getPlaylistId, id).remove();
        return CommonResult.success(ok);
    }

    // ── 歌单歌曲操作 ──

    @PostMapping("/{playlistId}/song/{songId}")
    @Operation(summary = "向歌单添加歌曲")
    public CommonResult<Boolean> addSong(@RequestAttribute("userId") Long userId,
                                         @PathVariable Long playlistId, @PathVariable Long songId,
                                         @RequestParam(defaultValue = "0") Integer sort) {
        // 校验歌单归属
        Playlist existing = playlistService.getById(playlistId);
        if (existing == null) return CommonResult.error("歌单不存在");
        if (!existing.getUserId().equals(userId)) {
            return CommonResult.error("无权操作该歌单");
        }
        PlaylistSong ps = new PlaylistSong();
        ps.setPlaylistId(playlistId);
        ps.setSongId(songId);
        ps.setSort(sort);
        boolean ok = playlistSongService.save(ps);
        if (ok) {
            playlistService.lambdaUpdate()
                    .eq(Playlist::getPlaylistId, playlistId)
                    .setSql("song_count = song_count + 1")
                    .update();
        }
        return CommonResult.success(ok);
    }

    @DeleteMapping("/{playlistId}/song/{songId}")
    @Operation(summary = "从歌单移除歌曲")
    public CommonResult<Boolean> removeSong(@RequestAttribute("userId") Long userId,
                                            @PathVariable Long playlistId, @PathVariable Long songId) {
        // 校验歌单归属
        Playlist existing = playlistService.getById(playlistId);
        if (existing == null) return CommonResult.error("歌单不存在");
        if (!existing.getUserId().equals(userId)) {
            return CommonResult.error("无权操作该歌单");
        }
        boolean ok = playlistSongService.lambdaUpdate()
                .eq(PlaylistSong::getPlaylistId, playlistId)
                .eq(PlaylistSong::getSongId, songId)
                .remove();
        if (ok) {
            playlistService.lambdaUpdate()
                    .eq(Playlist::getPlaylistId, playlistId)
                    .setSql("song_count = GREATEST(song_count - 1, 0)")
                    .update();
        }
        return CommonResult.success(ok);
    }

    // ── 推荐 / 热门 ──

    @GetMapping("/hot")
    @Operation(summary = "热门歌单", description = "按播放量 Top 50")
    public CommonResult<List<Playlist>> getHotPlaylists(@RequestParam(value = "page", defaultValue = "1") int page,
                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        // 限制 size 上限，防止请求过大导致性能问题
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 1);
        List<Playlist> list = playlistService.lambdaQuery()
                .eq(Playlist::getStatus, 1)
                .orderByDesc(Playlist::getPlayCount)
                .last("LIMIT " + (safePage - 1) * safeSize + ", " + safeSize)
                .list();
        return CommonResult.success(list);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "用户创建的歌单")
    public CommonResult<List<Playlist>> getUserPlaylists(@PathVariable Long userId) {
        List<Playlist> list = playlistService.lambdaQuery()
                .eq(Playlist::getUserId, userId)
                .orderByDesc(Playlist::getCreateTime)
                .list();
        return CommonResult.success(list);
    }
}
