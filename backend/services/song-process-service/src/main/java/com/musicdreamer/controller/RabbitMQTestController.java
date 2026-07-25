package com.musicdreamer.controller;

import com.musicdreamer.common.CommonResult;
import com.musicdreamer.dto.SongDTO;
import com.musicdreamer.service.SongEventPublisher;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitmq")
@Tag(name = "RabbitMQ测试", description = "消息队列测试接口")
public class RabbitMQTestController {

    private final SongEventPublisher songEventPublisher;

    public RabbitMQTestController(SongEventPublisher songEventPublisher) {
        this.songEventPublisher = songEventPublisher;
    }

    @PostMapping("/test/publish-song")
    @Operation(summary = "发送一条歌曲发布测试消息")
    public CommonResult<String> testPublish(@RequestBody SongDTO song) {
        songEventPublisher.publishSongCreated(song);
        return CommonResult.success("消息已发送: songId=" + song.getSongId());
    }
}
