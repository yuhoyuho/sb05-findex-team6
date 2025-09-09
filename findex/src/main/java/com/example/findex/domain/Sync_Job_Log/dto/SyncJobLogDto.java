package com.example.findex.domain.Sync_Job_Log.dto;

import lombok.Getter;
import lombok.Setter;

<<<<<<< HEAD
import java.time.LocalDate;
import java.time.LocalDateTime;

=======
>>>>>>> fc1c724 (feat: 지수 정보 연동 API)
@Getter
@Setter
public class SyncJobLogDto {

    private Long id;
    private String jobType;
<<<<<<< HEAD
    private Long indexInfoId;
    private LocalDate targetDate;
    private String worker;
    private LocalDateTime jobTime;
=======
    private int indexInfoId;
    private String targetDate;
    private String worker;
    private String jobTime;
>>>>>>> fc1c724 (feat: 지수 정보 연동 API)
    private String result;
}
