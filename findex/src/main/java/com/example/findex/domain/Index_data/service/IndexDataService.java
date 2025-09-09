package com.example.findex.domain.Index_data.service;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.dto.CursorPageResponseIndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.mapper.IndexDataMapper;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexDataService {

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataMapper indexDataMapper;

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


        if (cursor != null) {
            try {
                String decodedCursor = new String(Base64.getDecoder().decode(cursor));
                // 예: {"baseDate":"2024-06-01"}, {"marketPrice":123.45}, {"id":100}
                if ("baseDate".equals(sortField)) {
                    String dateStr = decodedCursor.replaceAll("[^0-9\\-]", "");
                    cursor = dateStr; // "2024-06-01"
                } else if ("marketPrice".equals(sortField) || "closingPrice".equals(sortField)) {
                    String numStr = decodedCursor.replaceAll("[^0-9.]", "");
                    cursor = numStr; // "123.45"
                } else {
                    String idStr = decodedCursor.replaceAll("[^0-9]", "");
                    cursor = idStr; // "100"
                }
            } catch (Exception e) {
                // 잘못된 커서는 무시
            }
        }
        if (cursor == null && idAfter != null) {
            cursor = idAfter.toString();
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(0, size + 1, sort);

        // 데이터 조회
        List<IndexData> dataList = indexDataRepository.findDataWithCondition(
                indexInfoId, startDate, endDate, sortField, sortDirection, cursor, pageable);

        // hasNext 확인
        boolean hasNext = dataList.size() > size;
        if (hasNext) {
            dataList = dataList.subList(0, size);
        }

        // MapStruct로 DTO 변환
        List<IndexDataDto> content = indexDataMapper.toDtoList(dataList);

        // 다음 커서 생성
        String nextCursor = null;
        Long nextIdAfter = null;
        if (hasNext && !dataList.isEmpty()) {
            Long lastId = dataList.get(dataList.size() - 1).getId();
            nextIdAfter = lastId;
            nextCursor = Base64.getEncoder().encodeToString(
                    String.format("{\"id\":%d}", lastId).getBytes()
            );
        }

        // 전체 개수 조회
        long totalElements = indexDataRepository.countDataWithCondition(indexInfoId, startDate, endDate);

        return new CursorPageResponseIndexDataDto(content, nextCursor, nextIdAfter, size, totalElements, hasNext);
    }

    public String exportToCsv(Long indexInfoId, LocalDate startDate, LocalDate endDate,
                              String sortField, String sortDirection) {
        List<IndexData> dataList = indexDataRepository.findDataWithCondition(
                indexInfoId, startDate, endDate, sortField, sortDirection, null, Pageable.ofSize(Integer.MAX_VALUE));

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

    private String formatNumber(BigDecimal value) {
        return value != null ? String.format("%.2f", value) : "0.00";
    }

    private String formatLong(Long value) {
        return value != null ? String.valueOf(value) : "0";
    }
}
