package com.example.findex.domain.Sync_Job_Log.entity;

import com.example.findex.common.base.BaseEntity;
import com.example.findex.common.base.JobResult;
import com.example.findex.common.base.JobType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
@Table(name = "integration_job")
public class SyncJobLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo; // 연동된 지수 정보

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "worker", nullable = false, length = 50)
    private String worker; // 작업자

    @Column(name = "job_time", nullable = false)
    private LocalDateTime jobTime; // 작업 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 10)
    private JobResult result; // 결과 (SUCCESS, FAILURE)

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // 상세 내용 (에러 메세지 등등)
}
