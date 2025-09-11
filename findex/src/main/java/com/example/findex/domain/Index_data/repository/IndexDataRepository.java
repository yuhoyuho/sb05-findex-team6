package com.example.findex.domain.Index_data.repository;

import com.example.findex.domain.Index_data.entity.IndexData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface IndexDataRepository extends JpaRepository<IndexData, Long> {
    List<IndexData> findAllByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(Long indexInfoId, LocalDate startDate, LocalDate endDate);
}