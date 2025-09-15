// com/example/findex/domain/Index_data/service/IndexDataService.java
package com.example.findex.domain.Index_data.service;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.dto.ChartDataPoint;
import com.example.findex.domain.Index_data.dto.CursorPageResponseIndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexChartResponse;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.dto.IndexPerformanceDto;
import com.example.findex.domain.Index_data.dto.PeriodType;
import com.example.findex.domain.Index_data.dto.RankedIndexPerformanceDto;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.mapper.IndexDataMapper;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

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
        if (indexDataRepository.existsByIndexInfoIdAndBaseDate(request.getIndexInfoId(), request.getBaseDate())) {
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
                indexInfoId, startDate, endDate, validSortField, validSortDirection, cursor, idAfter, size);

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

    @Transactional(readOnly = true)
    public String exportToCsv(Long indexInfoId, LocalDate startDate, LocalDate endDate,
                              String sortField, String sortDirection) {
        String validSortField = getValidSortField(sortField);
        String validSortDirection = getValidSortDirection(sortDirection);
        List<IndexData> dataList = indexDataRepository.findDataWithDynamicSort(
                indexInfoId, startDate, endDate, validSortField, validSortDirection, null, null, Integer.MAX_VALUE - 1);

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
        IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId)
                .orElseThrow(() -> new EntityNotFoundException("IndexInfo not found with id: " + indexInfoId));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = calculateDate(endDate, periodType);

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
                if (currentData.getClosingPrice() != null) {
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

        Collections.reverse(dataPoints);
        Collections.reverse(ma5DataPoints);
        Collections.reverse(ma20DataPoints);

        return new IndexChartResponse(indexInfoId, indexInfo.getIndexClassification(), indexInfo.getIndexName(), periodType,
                dataPoints, ma5DataPoints, ma20DataPoints);
    }

    public List<RankedIndexPerformanceDto> getRankedPerformance(Long indexInfoId, PeriodType periodType, int limit) {
        LocalDate end = indexDataRepository.findLatestBaseDate();
        if (end == null) return List.of();
        LocalDate start = calculateDate(end, periodType);

        List<IndexData> latestList = indexDataRepository.findLatestSnapshotInRange(start, end, null);
        if (latestList.isEmpty()) return List.of();

        Map<Long, IndexData> latestById = latestList.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), Function.identity(), (a, b) -> a));

        Set<Long> ids = latestById.keySet();
        List<IndexData> earliestList = indexDataRepository.findEarliestSnapshotInRange(start, end, ids);
        Map<Long, IndexData> earliestById = earliestList.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), Function.identity(), (a, b) -> a));

        List<IndexPerformanceDto> rows = new ArrayList<>();
        for (Map.Entry<Long, IndexData> e : latestById.entrySet()) {
            Long id = e.getKey();
            IndexData cur = e.getValue();
            BigDecimal curPrice = cur.getClosingPrice();
            if (curPrice == null) continue;

            IndexData past = earliestById.get(id);
            BigDecimal beforePrice = (past == null || cur.getBaseDate().equals(past.getBaseDate()) || past.getClosingPrice() == null)
                    ? curPrice
                    : past.getClosingPrice();

            BigDecimal versus = curPrice.subtract(beforePrice);
            BigDecimal rate = (beforePrice.compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            rows.add(IndexPerformanceDto.builder()
                    .indexInfoId(id)
                    .indexClassification(cur.getIndexInfo().getIndexClassification())
                    .indexName(cur.getIndexInfo().getIndexName())
                    .currentPrice(curPrice)
                    .beforePrice(beforePrice)
                    .versus(versus)
                    .fluctuationRate(rate)
                    .build());
        }

        rows.sort(Comparator.comparing(IndexPerformanceDto::getFluctuationRate,
                Comparator.nullsLast(BigDecimal::compareTo)).reversed());

        List<RankedIndexPerformanceDto> ranked = new ArrayList<>();
        int rank = 1;
        for (IndexPerformanceDto p : rows) {
            ranked.add(RankedIndexPerformanceDto.builder().rank(rank++).performance(p).build());
        }

        if (indexInfoId != null) {
            return ranked.stream()
                    .filter(r -> r.getPerformance().getIndexInfoId().equals(indexInfoId))
                    .toList();
        }
        return ranked.stream().limit(limit).toList();
    }

    public List<IndexPerformanceDto> getFavoritePerformance(PeriodType periodType) {
        LocalDate end = indexDataRepository.findLatestBaseDate();
        if (end == null) return List.of();
        LocalDate start = calculateDate(end, periodType);

        List<IndexData> latestAll = indexDataRepository.findLatestSnapshotInRange(start, end, null);
        if (latestAll.isEmpty()) return List.of();

        List<IndexData> favoriteLatest = latestAll.stream()
                .filter(d -> d.getIndexInfo().isFavorite())
                .toList();
        if (favoriteLatest.isEmpty()) return List.of();

        Map<Long, IndexData> latestById = favoriteLatest.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), Function.identity(), (a, b) -> a));

        Set<Long> favIds = latestById.keySet();
        List<IndexData> earliestList = indexDataRepository.findEarliestSnapshotInRange(start, end, favIds);
        Map<Long, IndexData> earliestById = earliestList.stream()
                .collect(Collectors.toMap(d -> d.getIndexInfo().getId(), Function.identity(), (a, b) -> a));

        List<IndexPerformanceDto> rows = new ArrayList<>();
        for (Map.Entry<Long, IndexData> e : latestById.entrySet()) {
            Long id = e.getKey();
            IndexData cur = e.getValue();
            BigDecimal curPrice = cur.getClosingPrice();
            if (curPrice == null) continue;

            IndexData past = earliestById.get(id);
            BigDecimal beforePrice = (past == null || cur.getBaseDate().equals(past.getBaseDate()) || past.getClosingPrice() == null)
                    ? curPrice
                    : past.getClosingPrice();

            BigDecimal versus = curPrice.subtract(beforePrice);
            BigDecimal rate = (beforePrice.compareTo(BigDecimal.ZERO) == 0)
                    ? BigDecimal.ZERO
                    : versus.divide(beforePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            rows.add(IndexPerformanceDto.builder()
                    .indexInfoId(id)
                    .indexClassification(cur.getIndexInfo().getIndexClassification())
                    .indexName(cur.getIndexInfo().getIndexName())
                    .currentPrice(curPrice)
                    .beforePrice(beforePrice)
                    .versus(versus)
                    .fluctuationRate(rate)
                    .build());
        }

        rows.sort(Comparator.comparing(IndexPerformanceDto::getFluctuationRate,
                Comparator.nullsLast(BigDecimal::compareTo)).reversed());
        return rows;
    }

    private LocalDate calculateDate(LocalDate endDate, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> endDate.minusDays(1);
            case WEEKLY -> endDate.minusDays(7);
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
        String json = String.format("{\"%s\":\"%s\",\"id\":%d}", validatedField, fieldValue, entity.getId());
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

    private String formatNumber(BigDecimal value) {
        return value != null ? String.format("%.2f", value) : "0.00";
    }

    private String formatLong(Long value) {
        return value != null ? String.valueOf(value) : "0";
    }
}
