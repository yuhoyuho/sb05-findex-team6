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

    // 동적 정렬
    PathBuilder<IndexInfo> entityPath = new PathBuilder<>(IndexInfo.class,"indexInfo");

    OrderSpecifier<?> orderSpecifier = new OrderSpecifier<>(
        sortDirection != null && sortDirection.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC,
        entityPath.get(sortField, Comparable.class)  // sortField = "id", "indexClassification" 등 엔티티 필드명
    );
    // 필터링 조건만
    if(filterField != null && filterValue != null) {
      BooleanBuilder filterBuilder = new BooleanBuilder();

      switch (filterField) {
        case "indexClassification" ->
            filterBuilder.and(indexInfo.indexClassification.eq(filterValue));
        case "sourceType" -> filterBuilder.and(indexInfo.sourceType.stringValue().eq(filterValue));
        case "indexName" -> filterBuilder.and(indexInfo.indexName.containsIgnoreCase(filterValue));
      }

      return queryFactory
          .selectFrom(indexInfo)
          .where(filterBuilder)
          .orderBy(orderSpecifier)
          .limit(size)
          .fetch();

    } else {
      // 일반 커서 페이지네이션
      BooleanBuilder cursorBuilder = new BooleanBuilder();
      if (cursor != null) {
        cursorBuilder.and(indexInfo.id.gt(cursor));
      }

      return queryFactory
          .selectFrom(indexInfo)
          .where(cursorBuilder)
          .orderBy(orderSpecifier)
          .limit(size)
          .fetch();
    }

  }
}
