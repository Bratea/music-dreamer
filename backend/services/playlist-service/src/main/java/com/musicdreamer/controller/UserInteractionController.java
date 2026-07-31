package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.entity.Notification;
import com.musicdreamer.entity.UserCollect;
import com.musicdreamer.entity.UserFollowSinger;
import com.musicdreamer.entity.UserHistory;
import com.musicdreamer.service.NotificationService;
import com.musicdreamer.service.UserCollectService;
import com.musicdreamer.service.UserFollowSingerService;
import com.musicdreamer.service.UserHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户交互接口（收藏、播放历史、消息通知、关注歌手）
 *
 * <p>路由说明：网关将 /api/me/** 原样转发到 playlist-service（不剥离前缀），
 * 因此本控制器直接以 /api/me 为根路径，对应前端调用的 /api/me/* 。
 */
@RestController
@RequestMapping("/api/me")
@Tag(name = "用户交互", description = "我的收藏/播放历史/消息通知/关注歌手")
public class UserInteractionController {

    private final UserCollectService userCollectService;
    private final UserHistoryService userHistoryService;
    private final NotificationService notificationService;
    private final UserFollowSingerService userFollowSingerService;

    public UserInteractionController(UserCollectService userCollectService,
                                     UserHistoryService userHistoryService,
                                     NotificationService notificationService,
                                     UserFollowSingerService userFollowSingerService) {
        this.userCollectService = userCollectService;
        this.userHistoryService = userHistoryService;
        this.notificationService = notificationService;
        this.userFollowSingerService = userFollowSingerService;
    }

    // ═══════════════════════════════════════
    // 收藏
    // ═══════════════════════════════════════

    @PostMapping("/collect")
    @Operation(summary = "收藏歌曲/歌单", description = "Body: {targetId, targetType}，已收藏则取消")
    public CommonResult<Map<String, Object>> collect(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Object> body) {
        Object tid = body.get("targetId");
        if (tid == null) return CommonResult.error("targetId 不能为空");
        Long targetId = Long.valueOf(tid.toString());
        Integer targetType = body.get("targetType") == null ? 1
                : Integer.valueOf(body.get("targetType").toString());
        boolean collected = userCollectService.toggleCollect(userId, targetId, targetType);
        return CommonResult.success(Map.of("targetId", targetId,
                "targetType", targetType, "collected", collected));
    }

    @DeleteMapping("/collect/{targetId}")
    @Operation(summary = "取消收藏")
    public CommonResult<Map<String, Object>> uncollect(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "1") Integer targetType) {
        userCollectService.toggleCollect(userId, targetId, targetType);
        return CommonResult.success(Map.of("targetId", targetId, "collected", false));
    }

    @GetMapping("/collect/{targetId}")
    @Operation(summary = "是否已收藏")
    public CommonResult<Map<String, Object>> isCollected(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "1") Integer targetType) {
        boolean collected = userCollectService.isCollected(userId, targetId, targetType);
        return CommonResult.success(Map.of("targetId", targetId, "collected", collected));
    }

    @GetMapping("/collections")
    @Operation(summary = "我的收藏列表", description = "可传 targetType=1(歌曲) / 2(歌单) 筛选")
    public CommonResult<List<UserCollect>> collections(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Integer targetType) {
        return CommonResult.success(userCollectService.getUserCollects(userId, targetType));
    }

    // ═══════════════════════════════════════
    // 播放历史
    // ═══════════════════════════════════════

    @GetMapping("/history")
    @Operation(summary = "我的播放历史", description = "默认返回最近 50 条")
    public CommonResult<List<UserHistory>> history(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "50") int limit) {
        return CommonResult.success(userHistoryService.getRecentHistory(userId, limit));
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空播放历史")
    public CommonResult<Boolean> clearHistory(@RequestAttribute("userId") Long userId) {
        userHistoryService.clearHistory(userId);
        return CommonResult.success(true);
    }

    // ═══════════════════════════════════════
    // 消息通知
    // ═══════════════════════════════════════

    @GetMapping("/notifications")
    @Operation(summary = "我的消息通知列表")
    public CommonResult<List<Notification>> notifications(
            @RequestAttribute("userId") Long userId) {
        return CommonResult.success(notificationService.getUnread(userId));
    }

    @PutMapping("/notification/{id}/read")
    @Operation(summary = "标记通知已读")
    public CommonResult<Boolean> markRead(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
        return CommonResult.success(true);
    }

    // ═══════════════════════════════════════
    // 关注歌手
    // ═══════════════════════════════════════

    @PostMapping("/follow")
    @Operation(summary = "关注/取消关注歌手", description = "Body: {singerId}，已关注则取消")
    public CommonResult<Map<String, Object>> follow(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, Long> body) {
        Long singerId = body.get("singerId");
        boolean following = userFollowSingerService.toggleFollow(userId, singerId);
        return CommonResult.success(Map.of("singerId", singerId, "following", following));
    }

    @DeleteMapping("/follow/{singerId}")
    @Operation(summary = "取消关注歌手")
    public CommonResult<Map<String, Object>> unfollow(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long singerId) {
        userFollowSingerService.toggleFollow(userId, singerId);
        return CommonResult.success(Map.of("singerId", singerId, "following", false));
    }

    @GetMapping("/follow/{singerId}")
    @Operation(summary = "是否已关注某歌手")
    public CommonResult<Map<String, Object>> isFollowing(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long singerId) {
        boolean following = userFollowSingerService.isFollowing(userId, singerId);
        return CommonResult.success(Map.of("singerId", singerId, "following", following));
    }

    @GetMapping("/following")
    @Operation(summary = "我关注的歌手列表")
    public CommonResult<List<UserFollowSinger>> following(
            @RequestAttribute("userId") Long userId) {
        return CommonResult.success(userFollowSingerService.getFollowing(userId));
    }
}
