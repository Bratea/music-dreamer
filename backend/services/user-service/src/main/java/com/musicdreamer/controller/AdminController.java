package com.musicdreamer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.AdminStatsResponse;
import com.musicdreamer.dto.DailyStat;
import com.musicdreamer.entity.Role;
import com.musicdreamer.entity.SingerApply;
import com.musicdreamer.dto.SongDTO;
import com.musicdreamer.entity.User;
import com.musicdreamer.entity.UserRole;
import com.musicdreamer.feign.SongFeignClient;
import com.musicdreamer.service.RoleService;
import com.musicdreamer.service.SingerApplyService;
import com.musicdreamer.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理后台", description = "[管理员] 用户/歌手/歌曲审核 + 数据统计")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;
    private final SingerApplyService singerApplyService;
    private final SongFeignClient songFeignClient;

    public AdminController(UserService userService, RoleService roleService,
                           SingerApplyService singerApplyService, SongFeignClient songFeignClient) {
        this.userService = userService;
        this.roleService = roleService;
        this.singerApplyService = singerApplyService;
        this.songFeignClient = songFeignClient;
    }

    // ═══════════════════════════════════════
    // 用户管理
    // ═══════════════════════════════════════

    @GetMapping("/users")
    @Operation(summary = "用户列表（分页）")
    public CommonResult<Map<String, Object>> userList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        long total = userService.count();
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("list", userService.lambdaQuery()
                .orderByDesc(User::getCreateTime)
                .page(com.baomidou.mybatisplus.extension.plugins.pagination.Page.of(page, size))
                .getRecords());
        return CommonResult.success(result);
    }

    @PutMapping("/user/{id}/disable")
    @Operation(summary = "封禁/解封用户 0=封禁 1=正常")
    public CommonResult<Boolean> disableUser(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setUserId(id);
        user.setStatus(status);
        return CommonResult.success(userService.updateById(user));
    }

    @DeleteMapping("/user/{id}")
    @Operation(summary = "删除用户")
    public CommonResult<Boolean> deleteUser(@PathVariable Long id) {
        return CommonResult.success(userService.removeById(id));
    }

    // ═══════════════════════════════════════
    // 歌手管理
    // ═══════════════════════════════════════

    @GetMapping("/singers")
    @Operation(summary = "歌手列表")
    public CommonResult<Map<String, Object>> singerList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 查 user_role 里带有 SINGER 角色的用户
        LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<>();
        // 暂时返回 singer 表全量（Singer 实体在 song-service，这里简化查 user_role）
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("page", page);
        result.put("size", size);
        result.put("list", List.of());
        return CommonResult.success(result);
    }

    // ═══════════════════════════════════════
    // 歌曲管理（通过 Feign 调用 song-service）
    // ═══════════════════════════════════════

    @GetMapping("/songs")
    @Operation(summary = "歌曲列表（含状态筛选）")
    public CommonResult<Map<String, Object>> songList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 通过 Feign 调用 song-service 获取歌曲列表
        CommonResult<List<SongDTO>> songResult = songFeignClient.listAll();
        List<SongDTO> allSongs = songResult.getData();
        Map<String, Object> result = new HashMap<>();
        result.put("total", allSongs != null ? allSongs.size() : 0);
        result.put("page", page);
        result.put("size", size);
        result.put("list", allSongs != null ? allSongs : List.of());
        return CommonResult.success(result);
    }

    @GetMapping("/songs/pending")
    @Operation(summary = "待审核歌曲列表")
    public CommonResult<List<SongDTO>> pendingSongs() {
        CommonResult<List<Song>> result = songFeignClient.pendingSongs();
        if (result.getData() == null) return CommonResult.success(List.of());
        List<SongDTO> dtoList = result.getData().stream().map(s -> {
            SongDTO dto = new SongDTO();
            dto.setSongId(s.getSongId());
            dto.setName(s.getName());
            dto.setSingerId(s.getSingerId());
            dto.setGenre(s.getGenre());
            dto.setLanguage(s.getLanguage());
            dto.setPlayCount(s.getPlayCount());
            dto.setStatus(s.getStatus());
            dto.setCreateTime(s.getCreateTime());
            return dto;
        }).toList();
        return CommonResult.success(dtoList);
    }

    @PutMapping("/song/{id}/audit/pass")
    @Operation(summary = "审核通过歌曲")
    public CommonResult<Void> auditPass(@PathVariable Long id,
                                        @RequestAttribute("userId") Long auditorId) {
        return songFeignClient.auditPass(id, auditorId != null ? auditorId : 1L);
    }

    @PutMapping("/song/{id}/audit/reject")
    @Operation(summary = "审核拒绝歌曲")
    public CommonResult<Void> auditReject(@PathVariable Long id,
                                          @RequestAttribute("userId") Long auditorId,
                                          @RequestParam String reason) {
        return songFeignClient.auditReject(id, auditorId != null ? auditorId : 1L, reason);
    }

    @PutMapping("/song/{id}/offline")
    @Operation(summary = "强制下架歌曲")
    public CommonResult<Void> offlineSong(@PathVariable Long id) {
        return songFeignClient.offlineSong(id);
    }

    // ═══════════════════════════════════════
    // 数据统计
    // ═══════════════════════════════════════

    @GetMapping("/stats/overview")
    @Operation(summary = "数据概览")
    public CommonResult<AdminStatsResponse> getOverview() {
        AdminStatsResponse resp = new AdminStatsResponse();
        resp.setTotalUsers(userService.count());
        CommonResult<Long> countResult = songFeignClient.count();
        resp.setTotalSongs(countResult.getData() != null ? countResult.getData() : 0L);
        CommonResult<List<Song>> listResult = songFeignClient.listAll();
        resp.setTotalPlayCount(listResult.getData() != null ? listResult.getData().stream()
                .mapToLong(s -> s.getPlayCount() != null ? s.getPlayCount() : 0)
                .sum() : 0L);
        resp.setTotalFollowers(0L);
        return CommonResult.success(resp);
    }

    @GetMapping("/stats/daily")
    @Operation(summary = "近7天每日统计")
    public CommonResult<List<DailyStat>> getDailyStats() {
        List<DailyStat> stats = java.util.stream.IntStream.rangeClosed(0, 6)
                .mapToObj(i -> {
                    LocalDate d = LocalDate.now().minusDays(i);
                    DailyStat s = new DailyStat();
                    s.setStatDate(d);
                    s.setNewUsers(0);
                    s.setNewSongs(0);
                    s.setTotalPlays(0L);
                    s.setActiveUsers(0);
                    return s;
                }).sorted((a, b) -> a.getStatDate().compareTo(b.getStatDate()))
                .toList();
        return CommonResult.success(stats);
    }
}
