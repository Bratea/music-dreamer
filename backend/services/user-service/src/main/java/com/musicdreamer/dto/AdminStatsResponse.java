package com.musicdreamer.dto;

import lombok.Data;

@Data
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalSongs;
    private Long totalPlayCount;
    private Long totalFollowers;
    private java.util.List<DailyStat> dailyStats;
}
