package com.example.findex.domain.Index_Info.service;

import com.example.findex.domain.Index_Info.dto.FavoriteIndexDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepository;
import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteIndexService {

    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    public List<FavoriteIndexDto> findFavoriteIndexSummaries() {
        List<IndexInfo> favoriteIndices = indexInfoRepository.findByFavorite(true);

        if (favoriteIndices.isEmpty()) {
            return Collections.emptyList();
        }

        return favoriteIndices.stream()
            .map(this::buildFavoriteIndexDto)
            .collect(Collectors.toList());
    }

    private FavoriteIndexDto buildFavoriteIndexDto(IndexInfo indexInfo) {
        // 가장 최신 IndexData를 찾기 위해 내림차순으로 정렬하여 첫 번째 항목을 가져옴
        Optional<IndexData> latestDataOptional = indexDataRepository
            .findAllByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(
                indexInfo.getId(), 
                LocalDate.now().minusYears(1), // 예시: 최근 1년 데이터 조회
                LocalDate.now()
            )
            .stream()
            .reduce((first, second) -> second); // 마지막 요소(가장 최신)를 찾음

        return latestDataOptional.map(latestData -> FavoriteIndexDto.builder()
            .id(indexInfo.getId())
            .indexName(indexInfo.getIndexName())
            .closingPrice(latestData.getClosingPrice())
            .versus(latestData.getVersus())
            .fluctuationRate(latestData.getFluctuationRate())
            .build())
            .orElse(null); // 데이터가 없는 경우 null을 반환하거나 기본 DTO를 생성할 수 있음
    }
}