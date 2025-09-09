package com.example.findex.domain.Sync_Job_Log.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class SyncJobLogDto {

    private Long id;
    private String jobType;
    private Long indexInfoId;
    private LocalDate targetDate;
    private String worker;
    private LocalDateTime jobTime;
    private String result;
}
