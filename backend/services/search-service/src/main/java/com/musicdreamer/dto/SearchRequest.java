package com.musicdreamer.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String keyword;
    private int page = 1;
    private int size = 20;
    private String type = "all"; // all, song, singer, playlist
}
