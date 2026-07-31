package com.musicdreamer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.entity.Song;
import com.musicdreamer.entity.SongAudit;
import com.musicdreamer.mapper.SongAuditMapper;
import com.musicdreamer.mapper.SongMapper;
import com.musicdreamer.service.SongAuditService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SongAuditServiceImpl extends ServiceImpl<SongAuditMapper, SongAudit>
        implements SongAuditService {

    private final SongMapper songMapper;

    public SongAuditServiceImpl(SongMapper songMapper) {
        this.songMapper = songMapper;
    }

    @Override
    public void submitAudit(Long songId) {
        SongAudit audit = new SongAudit();
        audit.setSongId(songId);
        audit.setStatus(0);
        audit.setAuditTime(LocalDateTime.now());
        save(audit);
    }

    @Override
    public void pass(Long auditId, Long auditorId) {
        SongAudit audit = getById(auditId);
        if (audit == null) throw new RuntimeException("审核记录不存在");
        audit.setStatus(1);
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditorId(auditorId);
        updateById(audit);
        // 同步歌曲状态为上架
        Song song = new Song();
        song.setSongId(audit.getSongId());
        song.setStatus(1);
        songMapper.updateById(song);
    }

    @Override
    public void reject(Long auditId, Long auditorId, String reason) {
        SongAudit audit = getById(auditId);
        if (audit == null) throw new RuntimeException("审核记录不存在");
        audit.setStatus(2);
        audit.setRejectReason(reason);
        audit.setAuditTime(LocalDateTime.now());
        audit.setAuditorId(auditorId);
        updateById(audit);
    }

    @Override
    public SongAudit getBySongId(Long songId) {
        // 一个 songId 可能有多条审核记录（如驳回后重新提审），取最新一条
        return lambdaQuery().eq(SongAudit::getSongId, songId)
                .orderByDesc(SongAudit::getAuditTime).last("LIMIT 1").one();
    }
}
