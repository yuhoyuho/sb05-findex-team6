package com.example.findex.domain.Sync_Job_Log.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncJobLogDto {

    private Long id;
    private String jobType;
    private Long indexInfoId;
    private String targetDate;
    private String worker;
    private String jobTime;
    private String result;
}
