package com.musicdreamer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 歌曲 DTO（用于跨服务 Feign 调用）
 * 与 song-service 的 Song 实体字段保持一致
 */
@Data
@Schema(description = "歌曲信息")
public class SongDTO {

    @Schema(description = "歌曲ID")
    private Long songId;

    @Schema(description = "歌曲名称")
    private String name;

    @Schema(description = "歌手ID")
    private Long singerId;

    @Schema(description = "歌手名称")
    private String singerName;

    @Schema(description = "专辑ID")
    private Long albumId;

    @Schema(description = "流派")
    private String genre;

    @Schema(description = "语言")
    private String language;

    @Schema(description = "歌词")
    private String lyrics;

    @Schema(description = "播放量")
    private Long playCount;

    @Schema(description = "状态：0=下架 1=上架 2=待审核")
    private Integer status;

    @Schema(description = "发布时间")
    private LocalDateTime releaseDate;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
