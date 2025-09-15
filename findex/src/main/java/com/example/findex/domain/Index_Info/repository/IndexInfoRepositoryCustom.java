package com.example.findex.domain.Index_Info.repository;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import java.util.List;

public interface IndexInfoRepositoryCustom {
  List<IndexInfo> findByCursorAndFilter(String cursor, int size, String sortField, String sortDirection ,String indexClassification, String indexName, Boolean favorite);
}
