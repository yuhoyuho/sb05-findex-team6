package com.example.findex.domain.Index_Info.repository.Impl;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.entity.QIndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<IndexInfo> findByCursorAndFilter(Long cursor, int size, String sortField ,String sortDirection, String filterField, String filterValue) {
    QIndexInfo indexInfo = QIndexInfo.indexInfo;

    BooleanBuilder builder = new BooleanBuilder();

    // 커서 조건
    if (cursor != null) {
      builder.and(indexInfo.id.gt(cursor));

    }

    // 필터 조건
    if (filterField != null && filterValue != null) {
      switch (filterField) {
        case "indexClassification" -> builder.and(indexInfo.indexClassification.eq(filterValue));
        case "sourceType" -> builder.and(indexInfo.sourceType.stringValue().eq(filterValue));
        case "indexName" -> builder.and(indexInfo.indexName.contains(filterValue));
        default -> {

        }

      }
    }

    // 동적 정렬
    PathBuilder<IndexInfo> entityPath = new PathBuilder<>(IndexInfo.class,"indexInfo");

    OrderSpecifier<?> orderSpecifier = new OrderSpecifier<>(
        sortDirection != null && sortDirection.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC,
        entityPath.get(sortField, Comparable.class)  // sortField = "id", "indexClassification" 등 엔티티 필드명
    );

    return queryFactory
        .selectFrom(indexInfo)
        .where(builder)
        .orderBy(orderSpecifier)
        .limit(size)
        .fetch();
  }
}
