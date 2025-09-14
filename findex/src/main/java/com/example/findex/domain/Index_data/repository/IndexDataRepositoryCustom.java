package com.example.findex.domain.Index_data.repository;

import com.example.findex.domain.Index_data.entity.IndexData;

import java.time.LocalDate;
import java.util.List;

public interface IndexDataRepositoryCustom {

    List<IndexData> findDataWithDynamicSort(
            Long indexInfoId,
            LocalDate startDate,
            LocalDate endDate,
            String sortField,
            String sortDirection,
            String cursor,
            Long idAfter,
            Integer size
    );

    List<IndexData> findLatestSnapshotAtOrBefore(LocalDate cutoff, Long indexInfoId);

    List<IndexData> findSnapshotAtExactDate(LocalDate date, Long indexInfoId);

    LocalDate findLatestBaseDate();
}