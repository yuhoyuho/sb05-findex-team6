package com.example.findex.common.openApi.service;

import com.example.findex.common.base.JobResult;
import com.example.findex.common.base.SourceType;
import com.example.findex.common.openApi.dto.IndexApiResponseDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import com.example.findex.domain.Sync_Job_Log.dto.SyncResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexSyncService {

    private final OpenApiService openApiService;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    @Transactional
    public void syncDailyData(LocalDate date) {
        // 💡 1. OpenApiService 호출 방식 변경
        // String이 아닌 DTO 객체를 직접 받습니다.
        IndexApiResponseDto responseDto = openApiService.fetchStockData(date);

        // API 응답이 null이거나, 응답 Body가 없거나, Item 목록이 없는 경우를 모두 체크
        if (responseDto == null || responseDto.getResponse() == null ||
                responseDto.getResponse().getBody() == null || responseDto.getResponse().getBody().getItems() == null) {
            log.warn("API에서 {} 날짜의 데이터를 가져오지 못했거나 응답 구조가 비어있습니다.", date);
            return;
        }

        // 💡 2. JSON 파싱 로직 제거
        // 이미 DTO로 변환되었으므로 ObjectMapper를 사용한 파싱 과정이 필요 없습니다.
        List<IndexApiResponseDto.Item> items = responseDto.getResponse().getBody().getItems().getItem();

        if (items != null && !items.isEmpty()) {
            log.info(">>>>> DTO 매핑 결과 샘플: {}", items.get(0));
        }

        if (items == null || items.isEmpty()) {
            log.info("{} 날짜에 동기화할 지수 데이터가 없습니다.", date);
            return;
        }

        // 각 데이터를 순회하며 DB에 저장 (이 로직은 그대로 유지됩니다)
        for (IndexApiResponseDto.Item item : items) {
            IndexInfo indexInfo = indexInfoRepository
                    .findByIndexNameAndIndexClassification(item.getIndexName(), item.getIndexClassification())
                    .orElseGet(() -> {
                        IndexInfo newInfo = createIndexInfoFromDto(item);
                        return indexInfoRepository.save(newInfo);
                    });

            IndexData indexData = createIndexDataFromDto(item, indexInfo);
            indexDataRepository.save(indexData);
            syncDailyDataAndWithResults(date);
        }
        log.info("{} 날짜의 지수 데이터 동기화가 성공적으로 완료되었습니다. ({}건 처리)", date, items.size());
    }

    /// 이 아래에서 본인이 맡은 부분 파싱하는 로직 작성하면 될 것 같습니다.

    /**
     * SyncJobLogService에서 사용하는 메서드
     * 데이터 동기화 수행, 각 데이터 처리 결과를 List로 반
     */
    @Transactional
    public List<SyncResult> syncDailyDataAndWithResults(LocalDate date) {
        IndexApiResponseDto response = openApiService.fetchStockData(date);
        List<SyncResult> result = new ArrayList<>();

        if(response == null || response.getResponse().getBody().getItems() == null || response.getResponse().getBody().getItems().getItem() == null) {
            log.warn("API에서 {} 날짜의 데이터를 가져오지 못했거나 응답 구조가 비어있습니다.", date);
            return result;
        }

        List<IndexApiResponseDto.Item> items = response.getResponse().getBody().getItems().getItem();
        if(items.isEmpty()) {
            log.info("{} 날짜에 동기화할 지수 데이터가 없습니다.", date);
            return result;
        }

        for (IndexApiResponseDto.Item item : items) {
            IndexInfo indexInfo = null;

            try {
                indexInfo = findOrCreateIndexInfo(item);

                IndexData indexData = createIndexDataFromDto(item, indexInfo);
                indexDataRepository.save(indexData);

                result.add(new SyncResult(JobResult.SUCCESS, indexInfo, "데이터 동기화 성공"));

            } catch(Exception e) {
                log.error("지수 '{}' 데이터 동기화 중 예외 발생", item.getIndexName(), e);
                result.add(new SyncResult(JobResult.FAILURE, indexInfo, e.getMessage()));
            }
        }

        log.info("{} 날짜의 지수 데이터 동기화가 완료되었습니다. (Total : {})", date, items.size());
        return result;
    }

    public IndexInfo findOrCreateIndexInfo(IndexApiResponseDto.Item item) {
        return indexInfoRepository
                .findByIndexNameAndIndexClassification(item.getIndexName(), item.getIndexClassification())
                .orElseGet(() -> indexInfoRepository.save(createIndexInfoFromDto(item)));
    }


    // DTO를 IndexInfo 엔티티로 변환하는 헬퍼 메서드 (팀원과 상의하여 구현) // 이 부분은 수정하셔도 될 것 같아요
    private IndexInfo createIndexInfoFromDto(IndexApiResponseDto.Item item) {
        return IndexInfo.builder()
                .indexClassification(item.getIndexClassification())
                .indexName(item.getIndexName())
                .employedItemsCount(item.getEmployedItemsCount())
                .basePointInTime(parseLocalDate(item.getBasePointTime())) // String -> LocalDate
                .baseIndex(parseBigDecimal(item.getBaseIndex()))       // String -> BigDecimal
                .sourceType(SourceType.OPEN_API) // API로부터 생성
                .favorite(false) // 기본값
                .build();
    }

    // DTO와 IndexInfo를 IndexData 엔티티로 변환하는 헬퍼 메서드 (팀원과 상의하여 구현) // 이 부분은 수정하셔도 될 것 같아요
    private IndexData createIndexDataFromDto(IndexApiResponseDto.Item item, IndexInfo indexInfo) {
        return IndexData.builder()
                .indexInfo(indexInfo) // 연관관계 설정
                .baseDate(parseLocalDate(item.getBaseDate()))
                .sourceType(SourceType.OPEN_API)
                .marketPrice(parseBigDecimal(item.getMarketPrice()))
                .closingPrice(parseBigDecimal(item.getClosingPrice()))
                .highPrice(parseBigDecimal(item.getHighPrice()))
                .lowPrice(parseBigDecimal(item.getLowPrice()))
                .versus(parseBigDecimal(item.getVersus()))
                .fluctuationRate(parseBigDecimal(item.getFluctuationRate()))
                .tradingQuantity(parseLong(item.getTradingQuantity()))
                .tradingPrice(parseLong(item.getTradingPrice()))
                .marketTotalAmount(parseLong(item.getMarketTotalAmount()))
                .build();
    }

    // --- 형 변환을 위한 유틸리티 메서드 ---
    private LocalDate parseLocalDate(String dateString) {
        if (!StringUtils.hasText(dateString)) return null;
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private BigDecimal parseBigDecimal(String numberString) {
        if (!StringUtils.hasText(numberString)) return null;
        // API에서 "1,234.56" 처럼 쉼표(,)가 포함된 숫자를 보내는 경우에 대비
        return new BigDecimal(numberString.replace(",", ""));
    }

    private Long parseLong(String numberString) {
        if (!StringUtils.hasText(numberString)) return null;
        return Long.parseLong(numberString.replace(",", ""));
    }

}
