package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.entity.Song;
import com.musicdreamer.entity.SongAudit;
import com.musicdreamer.service.SongAuditService;
import com.musicdreamer.service.SongService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理后台 - 歌曲审核管理", description = "[管理员] 歌曲审核 + 下架")
public class SongAuditController {

    private final SongAuditService songAuditService;
    private final SongService songService;

    public SongAuditController(SongAuditService songAuditService, SongService songService) {
        this.songAuditService = songAuditService;
        this.songService = songService;
    }

    // ── 审核相关 ──

    @GetMapping("/audit/song/pending")
    @Operation(summary = "待审核歌曲列表")
    public CommonResult<List<SongAudit>> pending() {
        return CommonResult.success(songAuditService.lambdaQuery()
                .eq(SongAudit::getStatus, 0)
                .orderByAsc(SongAudit::getAuditTime)
                .list());
    }

    @PutMapping("/audit/song/{auditId}/pass")
    @Operation(summary = "审核通过")
    public CommonResult<Void> pass(@PathVariable Long auditId,
                                    @RequestAttribute("userId") Long auditorId) {
        songAuditService.pass(auditId, auditorId);
        return CommonResult.success(null);
    }

    @PutMapping("/audit/song/{auditId}/reject")
    @Operation(summary = "审核拒绝")
    public CommonResult<Void> reject(@PathVariable Long auditId,
                                      @RequestAttribute("userId") Long auditorId,
                                      @RequestParam String reason) {
        songAuditService.reject(auditId, auditorId, reason);
        return CommonResult.success(null);
    }

    // ── 歌曲管理（供 user-service Feign 调用）──

    @GetMapping("/songs/pending")
    @Operation(summary = "待审核歌曲列表（按歌曲状态）")
    public CommonResult<List<Song>> pendingSongs() {
        return CommonResult.success(songService.lambdaQuery()
                .eq(Song::getStatus, 2)
                .orderByAsc(Song::getCreateTime)
                .list());
    }

    @PutMapping("/song/{id}/audit/pass")
    @Operation(summary = "审核通过歌曲")
    public CommonResult<Void> auditPass(@PathVariable Long id,
                                        @RequestAttribute("userId") Long auditorId) {
        Song song = new Song();
        song.setSongId(id);
        song.setStatus(1);
        songService.updateById(song);
        return CommonResult.success(null);
    }

    @PutMapping("/song/{id}/audit/reject")
    @Operation(summary = "审核拒绝歌曲")
    public CommonResult<Void> auditReject(@PathVariable Long id,
                                          @RequestAttribute("userId") Long auditorId,
                                          @RequestParam String reason) {
        Song song = new Song();
        song.setSongId(id);
        song.setStatus(0);
        songService.updateById(song);
        return CommonResult.success(null);
    }

    @PutMapping("/song/{id}/offline")
    @Operation(summary = "强制下架歌曲")
    public CommonResult<Void> offlineSong(@PathVariable Long id) {
        Song song = new Song();
        song.setSongId(id);
        song.setStatus(0);
        songService.updateById(song);
        return CommonResult.success(null);
    }
}
