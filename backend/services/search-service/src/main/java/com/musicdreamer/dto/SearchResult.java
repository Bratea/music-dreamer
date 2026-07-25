package com.musicdreamer.dto;

import lombok.Data;
import java.util.List;

@Data
public class SearchResult {
    private List<SongItem> songs;
    private List<SingerItem> singers;
    private List<PlaylistItem> playlists;
    private long total;
    private int page;
    private int size;

    @Data
    public static class SongItem {
        private Long songId;
        private String name;
        private String singerName;
        private String cover;
        private Integer playCount;
    }

    @Data
    public static class SingerItem {
        private Long singerId;
        private String name;
        private String avatar;
    }

    @Data
    public static class PlaylistItem {
        private Long playlistId;
        private String name;
        private String cover;
        private Integer playCount;
    }
}
