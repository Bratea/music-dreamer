package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.SingerApply;
import java.util.List;

public interface SingerApplyService extends IService<SingerApply> {
    SingerApply submitApply(Long userId, SingerApply apply);
    SingerApply getMyApply(Long userId);
    List<SingerApply> getPendingApplies();
    void approve(Long applyId, Long reviewerId);
    void reject(Long applyId, Long reviewerId, String reason);
}
