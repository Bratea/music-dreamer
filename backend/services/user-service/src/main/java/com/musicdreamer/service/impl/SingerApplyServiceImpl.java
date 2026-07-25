package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Role;
import com.musicdreamer.entity.SingerApply;
import com.musicdreamer.entity.UserRole;
import com.musicdreamer.mapper.SingerApplyMapper;
import com.musicdreamer.mapper.UserRoleMapper;
import com.musicdreamer.service.RoleService;
import com.musicdreamer.service.SingerApplyService;
import com.musicdreamer.service.UserRoleService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SingerApplyServiceImpl extends ServiceImpl<SingerApplyMapper, SingerApply>
        implements SingerApplyService {

    private final UserRoleService userRoleService;
    private final RoleService roleService;

    public SingerApplyServiceImpl(UserRoleService userRoleService, RoleService roleService) {
        this.userRoleService = userRoleService;
        this.roleService = roleService;
    }

    @Override
    public SingerApply submitApply(Long userId, SingerApply apply) {
        // 关闭已有未审核记录
        lambdaUpdate().eq(SingerApply::getUserId, userId)
                .eq(SingerApply::getStatus, 0)
                .setSql("status = 3")  // 3=废弃
                .update();
        apply.setUserId(userId);
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        save(apply);
        return apply;
    }

    @Override
    public SingerApply getMyApply(Long userId) {
        return lambdaQuery().eq(SingerApply::getUserId, userId)
                .orderByDesc(SingerApply::getApplyTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public List<SingerApply> getPendingApplies() {
        return lambdaQuery().eq(SingerApply::getStatus, 0)
                .orderByAsc(SingerApply::getApplyTime)
                .list();
    }

    @Override
    public void approve(Long applyId, Long reviewerId) {
        SingerApply apply = getById(applyId);
        if (apply == null) throw new RuntimeException("申请不存在");
        apply.setStatus(1);
        apply.setReviewTime(LocalDateTime.now());
        apply.setReviewerId(reviewerId);
        updateById(apply);
        // 给用户追加 SINGER 角色
        Role singerRole = roleService.lambdaQuery()
                .eq(Role::getRoleCode, "SINGER")
                .one();
        if (singerRole == null) throw new RuntimeException("SINGER 角色不存在");
        Long singerRoleId = singerRole.getRoleId();
        userRoleService.grantRole(apply.getUserId(), singerRoleId);
    }

    @Override
    public void reject(Long applyId, Long reviewerId, String reason) {
        SingerApply apply = getById(applyId);
        if (apply == null) throw new RuntimeException("申请不存在");
        apply.setStatus(2);
        apply.setRejectReason(reason);
        apply.setReviewTime(LocalDateTime.now());
        apply.setReviewerId(reviewerId);
        updateById(apply);
    }
}
