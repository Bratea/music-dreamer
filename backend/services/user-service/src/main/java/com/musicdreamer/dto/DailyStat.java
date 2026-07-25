package com.musicdreamer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DailyStat {
    private LocalDate statDate;
    private Integer newUsers;
    private Integer newSongs;
    private Long totalPlays;
    private Integer activeUsers;
}
