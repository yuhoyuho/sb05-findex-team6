package com.example.findex.domain.Sync_Job_Log.controller;

import com.example.findex.domain.Sync_Job_Log.dto.SyncJobLogDto;
import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;
import com.example.findex.domain.Sync_Job_Log.mapper.SyncJobLogMapper;
import com.example.findex.domain.Sync_Job_Log.service.SyncJobLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobLogController {

    private final SyncJobLogService syncJobLogService;
    private final SyncJobLogMapper syncJobLogMapper;

    /**
     * HTTP : POST
     * EndPoint : /api/sync-jobs/index-infos
     * 지수 정보 연동
     */
    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobLogDto>> syncIndexInfos(HttpServletRequest request) {

        String clientIp = getClientIp(request);

        // scheduler를 통해서 batch 작업 수행 시에는 http 요청이 없기 때문에
        // batch를 실행하는 서비스 코드에서 worker를 "SYSTEM"으로 명시
        List<SyncJobLog> jobLogs = syncJobLogService.syncAndLogLatestIndexData(clientIp);

        // entity -> dto
        List<SyncJobLogDto> response = syncJobLogMapper.toDtoList(jobLogs);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
