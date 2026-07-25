package com.musicdreamer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.entity.SongAudit;

public interface SongAuditService extends IService<SongAudit> {
    void submitAudit(Long songId);
    void pass(Long auditId, Long auditorId);
    void reject(Long auditId, Long auditorId, String reason);
    SongAudit getBySongId(Long songId);
}
