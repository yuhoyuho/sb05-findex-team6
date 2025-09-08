package com.example.findex;

import com.example.findex.common.openApi.service.IndexSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final IndexSyncService indexSyncService;
    @PostMapping("/sync")
    public ResponseEntity<String> syncDataManually(@RequestParam("date") String dateString) {
        log.info("수동 동기화 요청 수신: 날짜={}", dateString);
        try {
            // "yyyy-MM-dd" 형식의 문자열을 LocalDate 객체로 변환합니다.
            LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // IndexSyncService의 동기화 메서드를 직접 호출합니다.
            indexSyncService.syncDailyData(date);

            String successMessage = date + " 데이터 동기화 요청이 성공적으로 처리되었습니다.";
            log.info(successMessage);
            return ResponseEntity.ok(successMessage);

        } catch (Exception e) {
            String errorMessage = "동기화 실패: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.internalServerError().body(errorMessage);
        }
    }
}
