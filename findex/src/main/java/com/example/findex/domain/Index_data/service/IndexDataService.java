package com.example.findex.domain.Index_data.service;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.dto.ChartDataPoint;
import com.example.findex.domain.Index_data.dto.CursorPageResponseIndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexChartResponse;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.dto.PeriodType;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.mapper.IndexDataMapper;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class IndexDataService {

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataMapper indexDataMapper;

    @Transactional
    public IndexDataDto createIndexData(IndexDataCreateRequest request) {
        IndexInfo indexInfo = indexInfoRepository.findById(request.getIndexInfoId())
                .orElseThrow(() -> new IllegalArgumentException("지수를 찾을 수 없습니다: " + request.getIndexInfoId()));

        if (indexDataRepository.existsByIndexInfoIdAndBaseDate(
                request.getIndexInfoId(),
                request.getBaseDate())
        ) {
            throw new IllegalArgumentException("해당 지수의 날짜 데이터가 이미 존재합니다: " + request.getBaseDate());
        }

        IndexData indexData = indexDataMapper.toEntity(request, indexInfo);

        return indexDataMapper.toDto(indexDataRepository.save(indexData));
    }

    public IndexDataDto updateIndexData(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지수 데이터를 찾을 수 없습니다: " + id));

        indexDataMapper.updateEntityFromRequest(request, indexData);

        IndexData saved = indexDataRepository.save(indexData);
        return indexDataMapper.toDto(saved);
    }

    @Transactional
    public void deleteIndexData(Long id) {
        if (!indexDataRepository.existsById(id)) {
            throw new IllegalArgumentException("지수 데이터를 찾을 수 없습니다: " + id);
        }
        indexDataRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public CursorPageResponseIndexDataDto getIndexDataList(
            Long indexInfoId, LocalDate startDate, LocalDate endDate,
            Long idAfter, String cursor, String sortField, String sortDirection, Integer size) {

        String validSortField = getValidSortField(sortField);
        String validSortDirection = getValidSortDirection(sortDirection);

        List<IndexData> dataList = indexDataRepository.findDataWithDynamicSort(
                indexInfoId, startDate, endDate,
                validSortField, validSortDirection, cursor, idAfter, size);

        boolean hasNext = dataList.size() > size;
        if (hasNext) {
            dataList = dataList.subList(0, size);
        }

        List<IndexDataDto> content = indexDataMapper.toDtoList(dataList);

        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext && !dataList.isEmpty()) {
            IndexData lastEntity = dataList.get(dataList.size() - 1);
            nextIdAfter = lastEntity.getId();
            nextCursor = generateNextCursor(lastEntity, validSortField);
        }

        long totalElements = indexDataRepository.countDataWithCondition(indexInfoId, startDate, endDate);

        return new CursorPageResponseIndexDataDto(content, nextCursor, nextIdAfter, size, totalElements, hasNext);
    }

    private String getValidSortField(String sortField) {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "baseDate";
        }

        return switch (sortField) {
            case "baseDate", "closingPrice", "marketPrice", "highPrice", "lowPrice",
                 "tradingQuantity", "tradingPrice", "marketTotalAmount" -> sortField;
            default -> "baseDate";
        };
    }

    private String getValidSortDirection(String sortDirection) {
        return "desc".equalsIgnoreCase(sortDirection) ? "desc" : "asc";
    }

    private String generateNextCursor(IndexData entity, String sortField) {
        String validatedField = getValidSortField(sortField);
        Object fieldValue = extractFieldValue(entity, validatedField);

        String json = String.format("{\"%s\":\"%s\",\"id\":%d}",
                validatedField, fieldValue, entity.getId());
        return Base64.getEncoder().encodeToString(json.getBytes());
    }

    private Object extractFieldValue(IndexData entity, String sortField) {
        return switch (sortField) {
            case "baseDate" -> entity.getBaseDate();
            case "closingPrice" -> entity.getClosingPrice();
            case "marketPrice" -> entity.getMarketPrice();
            case "highPrice" -> entity.getHighPrice();
            case "lowPrice" -> entity.getLowPrice();
            case "tradingQuantity" -> entity.getTradingQuantity();
            case "tradingPrice" -> entity.getTradingPrice();
            case "marketTotalAmount" -> entity.getMarketTotalAmount();
            default -> entity.getId();
        };
    }

    @Transactional(readOnly = true)
    public String exportToCsv(Long indexInfoId, LocalDate startDate, LocalDate endDate,
                              String sortField, String sortDirection) {

        String validSortField = getValidSortField(sortField);
        String validSortDirection = getValidSortDirection(sortDirection);

        // QueryDSL로 모든 데이터 조회 (cursor 없이, 무제한 조회)
        List<IndexData> dataList = indexDataRepository.findDataWithDynamicSort(
                indexInfoId, startDate, endDate,
                validSortField, validSortDirection, null, null, Integer.MAX_VALUE-1);

        StringBuilder csv = new StringBuilder();
        csv.append("지수분류,지수명,기준일자,소스타입,시가,종가,고가,저가,대비,등락률,거래량,거래대금,상장시가총액\n");

        for (IndexData data : dataList) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    data.getIndexInfo().getIndexClassification(),
                    data.getIndexInfo().getIndexName(),
                    data.getBaseDate(),
                    data.getSourceType(),
                    formatNumber(data.getMarketPrice()),
                    formatNumber(data.getClosingPrice()),
                    formatNumber(data.getHighPrice()),
                    formatNumber(data.getLowPrice()),
                    formatNumber(data.getVersus()),
                    formatNumber(data.getFluctuationRate()),
                    formatLong(data.getTradingQuantity()),
                    formatLong(data.getTradingPrice()),
                    formatLong(data.getMarketTotalAmount())
            ));
        }

        return csv.toString();
    }

    @Transactional(readOnly = true)
    public IndexChartResponse getIndexChart(Long indexInfoId, PeriodType periodType) {
        // 1. 지수 정보 조회
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found with id: " + indexInfoId));

        // 2. 기간에 따른 데이터 조회 날짜 범위 설정
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateStartDate(endDate, periodType);

        // 3. 데이터 조회 - ASC로 조회 (이동평균 계산을 위해)
        List<IndexData> indexDataList = indexDataRepository.findAllByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
                indexInfoId, startDate.minusDays(30), endDate
        );

        if (indexDataList.isEmpty()) {
            return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        // 4. DTO 변환 및 이동평균선 계산 (ASC 순서로)
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

            // 5일 이동평균선 계산
            if (i >= 4) {
                double ma5 = calculateMovingAverage(indexDataList, i, 5);
                if (!currentData.getBaseDate().isBefore(startDate)) {
                    ma5DataPoints.add(new ChartDataPoint(currentData.getBaseDate(), ma5));
                }
            }

            // 20일 이동평균선 계산
            if (i >= 19) {
                double ma20 = calculateMovingAverage(indexDataList, i, 20);
                if (!currentData.getBaseDate().isBefore(startDate)) {
                    ma20DataPoints.add(new ChartDataPoint(currentData.getBaseDate(), ma20));
                }
            }
        }

        // 5. 응답 데이터를 DESC 순서로 정렬
        Collections.reverse(dataPoints);
        Collections.reverse(ma5DataPoints);
        Collections.reverse(ma20DataPoints);

        return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                dataPoints, ma5DataPoints, ma20DataPoints);
    }

    private LocalDate calculateStartDate(LocalDate endDate, PeriodType periodType) {
        return switch (periodType) {
            case YEARLY -> endDate.minusYears(1);
            case QUARTERLY -> endDate.minusMonths(3);
            default -> endDate.minusMonths(1);
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

    private String formatNumber(BigDecimal value) {
        return value != null ? String.format("%.2f", value) : "0.00";
    }

    private String formatLong(Long value) {
        return value != null ? String.valueOf(value) : "0";
    }
}
