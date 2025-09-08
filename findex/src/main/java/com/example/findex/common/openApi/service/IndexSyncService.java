package com.example.findex.common.openApi.service;

import com.example.findex.common.base.SourceType;
import com.example.findex.common.openApi.dto.IndexApiResponseDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexSyncService {

    private final OpenApiService openApiService;
    private final ObjectMapper objectMapper;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    @Transactional
    public void syncDailyData(LocalDate date) {
        // OpenApiService 호출
        String json = openApiService.fetchIndexDataAsString(date);
        if(json == null) {
            log.warn("API에서 {} 날짜의 데이터를 가져오지 못했습니다.", date);
            return;
        }

        try {
            // 받아온 JSON을 DTO로 파싱
            IndexApiResponseDto responseDto = objectMapper.readValue(json, IndexApiResponseDto.class);
            List<IndexApiResponseDto.Item> items = responseDto.getResponse().getBody().getItems().getItem();

            // 각 데이터를 순회하며 DB에 저장
            for (IndexApiResponseDto.Item item : items) {
                // 지수 정보(IndexInfo)를 DB에서 찾거나, 없으면 새로 생성하여 저장
                IndexInfo indexInfo = indexInfoRepository
                        .findByIndexNameAndIndexClassification(item.getIndexName(), item.getIndexClassification())
                        .orElseGet(() -> {
                            IndexInfo newInfo = createIndexInfoFromDto(item); // DTO -> Entity
                            return indexInfoRepository.save(newInfo);
                        });

                // 일별 데이터(IndexData) 엔티티를 생성하고 저장
                IndexData indexData = createIndexDataFromDto(item, indexInfo); // DTO -> Entity
                indexDataRepository.save(indexData);
            }
            log.info("{} 날짜의 지수 데이터 동기화가 성공적으로 완료되었습니다.", date);

        } catch (JsonProcessingException e) {
            log.error("{} 날짜의 지수 데이터 파싱 중 오류가 발생했습니다. 응답 내용: {}", date, json, e);
        }
    }

    /// 이 아래에서 본인이 맡은 부분 파싱하는 로직 작성하면 될 것 같습니다.

    // DTO를 IndexInfo 엔티티로 변환하는 헬퍼 메서드 (팀원과 상의하여 구현) // 이 부분은 수정하셔도 될 것 같아요
    private IndexInfo createIndexInfoFromDto(IndexApiResponseDto.Item item) {
        return IndexInfo.builder()
                .indexClassification(item.getIndexClassification())
                .indexName(item.getIndexName())
                .employedItemsCount(item.getEmployedItemsCount())
                .basePointInTime(parseLocalDate(item.getBasePointTime())) // String -> LocalDate
                .baseIndex(parseBigDecimal(item.getBaseIndex()))       // String -> BigDecimal
                .sourceType(SourceType.OpenAPI) // API로부터 생성
                .favorite(false) // 기본값
                .build();
    }

    // DTO와 IndexInfo를 IndexData 엔티티로 변환하는 헬퍼 메서드 (팀원과 상의하여 구현) // 이 부분은 수정하셔도 될 것 같아요
    private IndexData createIndexDataFromDto(IndexApiResponseDto.Item item, IndexInfo indexInfo) {
        return IndexData.builder()
                .indexInfo(indexInfo) // 연관관계 설정
                .baseDate(parseLocalDate(item.getBaseDate()))
                .sourceType(SourceType.OpenAPI)
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
