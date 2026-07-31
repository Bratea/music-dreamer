package com.musicdreamer.feign;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.SongDTO;
import com.musicdreamer.entity.Song;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 歌曲服务 Feign 客户端
 * 用于 user-service 跨服务调用 song-service 的歌曲相关接口
 */
@FeignClient(name = "song-service", contextId = "songFeignClient")
public interface SongFeignClient {

    /**
     * 查询待审核歌曲列表
     */
    @GetMapping("/api/admin/songs/pending")
    CommonResult<List<Song>> pendingSongs();

    /**
     * 审核通过歌曲
     */
    @PutMapping("/api/admin/song/{id}/audit/pass")
    CommonResult<Void> auditPass(@PathVariable("id") Long id,
                                 @RequestParam("auditorId") Long auditorId);

    /**
     * 审核拒绝歌曲
     */
    @PutMapping("/api/admin/song/{id}/audit/reject")
    CommonResult<Void> auditReject(@PathVariable("id") Long id,
                                   @RequestParam("auditorId") Long auditorId,
                                   @RequestParam("reason") String reason);

    /**
     * 强制下架歌曲
     */
    @PutMapping("/api/admin/song/{id}/offline")
    CommonResult<Void> offlineSong(@PathVariable("id") Long id);

    /**
     * 更新歌曲信息
     */
    @PutMapping("/api/song")
    CommonResult<Void> updateSong(@RequestBody SongDTO song);

    /**
     * 统计歌曲总数
     */
    @GetMapping("/api/song/count")
    CommonResult<Long> count();

    /**
     * 查询所有歌曲（用于统计播放量）
     */
    @GetMapping("/api/song/list/all")
    CommonResult<List<Song>> listAll();

    /**
     * 查询总播放量（聚合查询，避免加载全表）
     */
    @GetMapping("/api/song/total-play-count")
    CommonResult<Long> totalPlayCount();
}
