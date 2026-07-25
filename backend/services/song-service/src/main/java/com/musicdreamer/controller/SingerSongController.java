package com.musicdreamer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.SongPublishRequest;
import com.musicdreamer.entity.Singer;
import com.musicdreamer.entity.Song;
import com.musicdreamer.mapper.SingerMapper;
import com.musicdreamer.service.SongAuditService;
import com.musicdreamer.service.event.SongEventPublisher;
import com.musicdreamer.service.SongService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/singer/song")
@Tag(name = "歌手歌曲管理", description = "歌手上传发布歌曲、管理我的歌曲")
public class SingerSongController {

    private final SongService songService;
    private final SongAuditService songAuditService;
    private final SongEventPublisher songEventPublisher;
    private final SingerMapper singerMapper;

    public SingerSongController(SongService songService, SongAuditService songAuditService,
                                SongEventPublisher songEventPublisher, SingerMapper singerMapper) {
        this.songService = songService;
        this.songAuditService = songAuditService;
        this.songEventPublisher = songEventPublisher;
        this.singerMapper = singerMapper;
    }

    /**
     * 根据当前登录用户ID解析对应的歌手ID。
     * 若该用户尚无歌手档案，则自动创建一个（兼容审核通过后未生成歌手记录的场景）。
     */
    private Long resolveSingerId(Long userId) {
        Singer singer = singerMapper.selectOne(new LambdaQueryWrapper<Singer>().eq(Singer::getUserId, userId));
        if (singer != null) {
            return singer.getSingerId();
        }
        Singer newSinger = new Singer();
        newSinger.setName("歌手_" + userId);
        newSinger.setUserId(userId);
        newSinger.setStatus(1);
        singerMapper.insert(newSinger);
        return newSinger.getSingerId();
    }

    @PostMapping("/publish")
    @Operation(summary = "发布歌曲（自动进入审核队列 + 发送MQ异步处理消息）")
    public CommonResult<Song> publish(@RequestAttribute("userId") Long userId,
                                       @Valid @RequestBody SongPublishRequest req) {
        Long singerId = resolveSingerId(userId);
        Song song = new Song();
        song.setName(req.getName());
        song.setSingerId(singerId);
        song.setAlbumId(req.getAlbumId());
        song.setDuration(req.getDuration());
        song.setUrl(req.getUrl());
        song.setCover(req.getCover());
        song.setLyrics(req.getLyrics());
        song.setDescription(req.getDescription());
        song.setGenre(req.getGenre());
        song.setLanguage(req.getLanguage());
        song.setReleaseDate(req.getReleaseDate());
        song.setPlayCount(0);
        song.setLikeCount(0);
        song.setStatus(2);  // 2 = 审核中
        songService.save(song);
        songAuditService.submitAudit(song.getSongId());

        // 🐰 发送 RabbitMQ 消息：歌曲发布事件
        try {
            songEventPublisher.publishSongCreated(song);
        } catch (Exception e) {
            // MQ 发送失败不影响主流程（记录日志即可）
            System.err.println("[MQ] 发送歌曲发布消息失败: " + e.getMessage());
        }

        return CommonResult.success(song);
    }

    @GetMapping("/my")
    @Operation(summary = "我的歌曲列表（根据当前登录用户自动匹配歌手）")
    public CommonResult<List<Song>> mySongs(@RequestAttribute("userId") Long userId,
                                             @RequestParam(required = false) Integer status) {
        Long singerId = resolveSingerId(userId);
        var qw = new LambdaQueryWrapper<Song>()
                .eq(Song::getSingerId, singerId)
                .orderByDesc(Song::getCreateTime);
        if (status != null) qw.eq(Song::getStatus, status);
        return CommonResult.success(songService.list(qw));
    }

    @PutMapping("/{id}/edit")
    @Operation(summary = "编辑歌曲信息（仅审核中的歌曲可改）")
    public CommonResult<Boolean> edit(@PathVariable Long id, @RequestBody Song song) {
        song.setSongId(id);
        if (song.getStatus() == null) {
            Song existing = songService.getById(id);
            song.setStatus(existing.getStatus());
        }
        return CommonResult.success(songService.updateById(song));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌曲")
    public CommonResult<Boolean> remove(@PathVariable Long id) {
        return CommonResult.success(songService.removeById(id));
    }

    @PutMapping("/{id}/offline")
    @Operation(summary = "下架歌曲")
    public CommonResult<Boolean> offline(@PathVariable Long id) {
        Song song = new Song();
        song.setSongId(id);
        song.setStatus(0);
        return CommonResult.success(songService.updateById(song));
    }

    @PutMapping("/{id}/online")
    @Operation(summary = "重新上架（重新提审）")
    public CommonResult<Boolean> online(@PathVariable Long id) {
        Song song = new Song();
        song.setSongId(id);
        song.setStatus(2);
        songService.updateById(song);
        songAuditService.submitAudit(id);
        return CommonResult.success(true);
    }
}
