package com.musicdreamer.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SongPublishRequest {
    @NotBlank(message = "歌曲名不能为空")
    private String name;
    private Long singerId;
    private Long albumId;
    private Integer duration;
    @NotBlank(message = "音频URL不能为空")
    private String url;
    private String cover;
    private String lyrics;
    private String description;
    private String genre;
    private String language;
    private String releaseDate;
}
