package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.SingerApplyRequest;
import com.musicdreamer.entity.SingerApply;
import com.musicdreamer.service.SingerApplyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/singer")
@Tag(name = "歌手认证", description = "歌手申请、我的认证状态、管理端审核")
public class SingerApplyController {

    private final SingerApplyService singerApplyService;

    public SingerApplyController(SingerApplyService singerApplyService) {
        this.singerApplyService = singerApplyService;
    }

    @PostMapping("/apply")
    @Operation(summary = "提交歌手认证申请")
    public CommonResult<SingerApply> submitApply(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SingerApplyRequest request) {
        SingerApply apply = new SingerApply();
        apply.setRealName(request.getRealName());
        apply.setIdCard(request.getIdCard());
        apply.setAvatar(request.getAvatar());
        apply.setIntro(request.getIntro());
        apply.setGender(request.getGender());
        apply.setCountry(request.getCountry());
        apply.setBirthday(request.getBirthday());
        return CommonResult.success(singerApplyService.submitApply(userId, apply));
    }

    @GetMapping("/apply/my")
    @Operation(summary = "查看我的歌手认证状态")
    public CommonResult<SingerApply> getMyApply(@RequestAttribute("userId") Long userId) {
        return CommonResult.success(singerApplyService.getMyApply(userId));
    }

    // ── 管理端 ──

    @GetMapping("/admin/applies")
    @Operation(summary = "[管理员] 获取待审核认证列表")
    public CommonResult<List<SingerApply>> getPendingApplies() {
        return CommonResult.success(singerApplyService.getPendingApplies());
    }

    @PutMapping("/admin/apply/{id}/approve")
    @Operation(summary = "[管理员] 审核通过歌手认证")
    public CommonResult<Void> approve(@PathVariable Long id,
                                       @RequestAttribute("userId") Long reviewerId) {
        singerApplyService.approve(id, reviewerId);
        return CommonResult.success(null);
    }

    @PutMapping("/admin/apply/{id}/reject")
    @Operation(summary = "[管理员] 拒绝歌手认证")
    public CommonResult<Void> reject(@PathVariable Long id,
                                      @RequestAttribute("userId") Long reviewerId,
                                      @RequestParam String reason) {
        singerApplyService.reject(id, reviewerId, reason);
        return CommonResult.success(null);
    }
}
