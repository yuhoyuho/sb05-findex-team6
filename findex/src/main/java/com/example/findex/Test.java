package com.example.findex;

import com.example.findex.common.openApi.service.IndexSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class Test implements CommandLineRunner {

    private final IndexSyncService indexSyncService;

    @Override
    public void run(String... args) throws Exception {
        log.info("======================================================");
        log.info("🚀 API 및 DB 동기화 테스트를 시작합니다...");
        log.info("======================================================");

        // 💡 테스트하고 싶은 날짜를 지정합니다.
        // 공휴일이나 주말에는 데이터가 없을 수 있으니, 가장 최근의 평일로 설정하는 것이 좋습니다.
        LocalDate testDate = LocalDate.of(2025, 9, 5); // 예: 2025년 9월 5일 (금요일)

        try {
            // IndexSyncService의 메서드를 직접 호출합니다.
            indexSyncService.syncDailyData(testDate);
            log.info("✅ 테스트가 성공적으로 실행되었습니다. 위 로그를 확인해주세요.");
        } catch (Exception e) {
            log.error("❌ 테스트 실행 중 예외가 발생했습니다.", e);
        }

        log.info("======================================================");
        log.info("👋 테스트가 종료되었습니다.");
        log.info("======================================================");
    }
}
