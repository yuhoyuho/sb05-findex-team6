package com.example.findex.domain.Index_data.service;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.dto.ChartDataPoint;
import com.example.findex.domain.Index_data.dto.IndexChartResponse;
import com.example.findex.domain.Index_data.dto.PeriodType;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexDataService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    public IndexChartResponse getIndexChart(Long indexInfoId, PeriodType periodType) {
        // 1. 지수 정보 조회
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found with id: " + indexInfoId));

        // 2. 기간에 따른 데이터 조회 날짜 범위 설정
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, periodType);

        // 3. 데이터 조회 (이동평균선 계산을 위해 20일치 데이터 추가 조회)
        List<IndexData> indexDataList = indexDataRepository.findAllByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
                indexInfoId, startDate.minusDays(30), endDate // 넉넉하게 30일 이전 데이터부터 조회
        );

        if (indexDataList.isEmpty()) {
            return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        // 4. DTO 변환 및 이동평균선 계산
        List<ChartDataPoint> dataPoints = new ArrayList<>();
        List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
        List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

        for (int i = 0; i < indexDataList.size(); i++) {
            IndexData currentData = indexDataList.get(i);

            // 차트에 표시될 데이터 (조회 시작일 이후)
            if (!currentData.getBaseDate().isBefore(startDate)) {
                if(currentData.getClosingPrice() != null) {
                    dataPoints.add(new ChartDataPoint(currentData.getBaseDate(), currentData.getClosingPrice().doubleValue()));
                }
            }

            // 5일 이동평균선 계산 (최소 5일치 데이터 필요)
            if (i >= 4) {
                double ma5 = calculateMovingAverage(indexDataList, i, 5);
                if (!currentData.getBaseDate().isBefore(startDate)) {
                    ma5DataPoints.add(new ChartDataPoint(currentData.getBaseDate(), ma5));
                }
            }

            // 20일 이동평균선 계산 (최소 20일치 데이터 필요)
            if (i >= 19) {
                double ma20 = calculateMovingAverage(indexDataList, i, 20);
                 if (!currentData.getBaseDate().isBefore(startDate)) {
                    ma20DataPoints.add(new ChartDataPoint(currentData.getBaseDate(), ma20));
                }
            }
        }

        return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                dataPoints, ma5DataPoints, ma20DataPoints);
    }

    @Transactional
    public Long setupTestData() {
        // 1. Clean up previous data
        indexDataRepository.deleteAllInBatch();
        indexInfoRepository.deleteAllInBatch();

        // 2. Create IndexInfo
        IndexInfo indexInfo = IndexInfo.builder()
                .indexClassification("Test Classification")
                .indexName("Test Index")
                .employedItemsCount(10)
                .basePointInTime(LocalDate.now().minusYears(1))
                .baseIndex(new BigDecimal("1000.00"))
                .favorite(false)
                .sourceType(SourceType.USER)
                .build();
        IndexInfo savedIndexInfo = indexInfoRepository.save(indexInfo);
        Long indexId = savedIndexInfo.getId();

        // 3. Create IndexData for the last 30 days
        LocalDate today = LocalDate.now();
        Random random = new Random();
        List<IndexData> dataList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            IndexData indexData = IndexData.builder()
                .indexInfo(savedIndexInfo)
                .baseDate(today.minusDays(i))
                .closingPrice(BigDecimal.valueOf(1000 + random.nextDouble() * 100))
                .sourceType(SourceType.USER)
                .build();
            dataList.add(indexData);
        }
        indexDataRepository.saveAll(dataList);

        return indexId;
    }

    private LocalDate calculateStartDate(LocalDate endDate, PeriodType periodType) {
        return switch (periodType) {
            case WEEKLY -> endDate.minusWeeks(1);
            case MONTHLY -> endDate.minusMonths(1);
            default -> endDate.minusDays(7); // DAILY 포함
        };
    }

    private double calculateMovingAverage(List<IndexData> dataList, int currentIndex, int days) {
        return dataList.subList(currentIndex - days + 1, currentIndex + 1)
                .stream()
                .filter(d -> d.getClosingPrice() != null)
                .mapToDouble(d -> d.getClosingPrice().doubleValue())
                .average()
                .orElse(0.0);
    }
}
