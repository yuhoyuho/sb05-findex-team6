package com.example.findex.domain.Sync_Job_Log.repository;

import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobLogRepository extends JpaRepository<SyncJobLog, Long> {
}
