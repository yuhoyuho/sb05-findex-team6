package com.example.findex.domain.Index_data.service;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.dto.*;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IndexDataService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    public IndexChartResponse getIndexChart(Long indexInfoId, PeriodType periodType) {
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found with id: " + indexInfoId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, periodType);

        List<IndexData> indexDataList = indexDataRepository.findAllByIndexInfo_IdAndBaseDateBetweenOrderByBaseDateAsc(
                indexInfoId, startDate.minusDays(30), endDate
        );

        if (indexDataList.isEmpty()) {
            return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        List<ChartDataPoint> dataPoints = new ArrayList<>();
        List<ChartDataPoint> ma5DataPoints = new ArrayList<>();
        List<ChartDataPoint> ma20DataPoints = new ArrayList<>();

        for (int i = 0; i < indexDataList.size(); i++) {
            IndexData currentData = indexDataList.get(i);

            if (!currentData.getBaseDate().isBefore(startDate)) {
                if(currentData.getClosingPrice() != null) {
                    dataPoints.add(new ChartDataPoint(currentData.getBaseDate(), currentData.getClosingPrice().doubleValue()));
                }
            }

            if (i >= 4) {
                double ma5 = calculateMovingAverage(indexDataList, i, 5);
                if (!currentData.getBaseDate().isBefore(startDate)) {
                    ma5DataPoints.add(new ChartDataPoint(currentData.getBaseDate(), ma5));
                }
            }

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

    public IndexPerformanceRankingResponseDto getPerformanceRanking() {
        List<IndexInfo> allIndices = indexInfoRepository.findAll();
        List<IndexPerformanceRankingDto> performanceList = new ArrayList<>();

        for (IndexInfo indexInfo : allIndices) {
            Page<IndexData> dataPage = indexDataRepository.findTop2ByIndexInfoOrderByBaseDateDesc(indexInfo, PageRequest.of(0, 2));

            if (dataPage.getContent().size() < 2) {
                continue;
            }

            IndexData latestData = dataPage.getContent().get(0);
            IndexData previousData = dataPage.getContent().get(1);

            BigDecimal change = latestData.getClosingPrice().subtract(previousData.getClosingPrice());
            double changePercent = 0.0;
            if (previousData.getClosingPrice().compareTo(BigDecimal.ZERO) != 0) {
                changePercent = change.divide(previousData.getClosingPrice(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).doubleValue();
            }

            performanceList.add(IndexPerformanceRankingDto.builder()
                    .symbol(indexInfo.getIndexName())
                    .name(indexInfo.getIndexName())
                    .currentPrice(latestData.getClosingPrice())
                    .change(change)
                    .changePercent(changePercent)
                    .build());
        }

        performanceList.sort(Comparator.comparing(IndexPerformanceRankingDto::getChangePercent).reversed());

        List<IndexPerformanceRankingDto> rankedList = new ArrayList<>();
        for (int i = 0; i < performanceList.size(); i++) {
            IndexPerformanceRankingDto dto = performanceList.get(i);
            rankedList.add(IndexPerformanceRankingDto.builder()
                    .ranking(i + 1)
                    .symbol(dto.getSymbol())
                    .name(dto.getName())
                    .currentPrice(dto.getCurrentPrice())
                    .change(dto.getChange())
                    .changePercent(dto.getChangePercent())
                    .build());
        }

        return new IndexPerformanceRankingResponseDto(rankedList);
    }

    @Transactional
    public Long setupTestData() {
        indexDataRepository.deleteAllInBatch();
        indexInfoRepository.deleteAllInBatch();

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
            default -> endDate.minusDays(7);
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
