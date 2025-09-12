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

@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<IndexInfo> findByCursorAndFilter(Long cursor, int size, String sortField,
      String sortDirection, String indexClassification, String indexName, Boolean favorite) {

    QIndexInfo indexInfo = QIndexInfo.indexInfo;
    BooleanBuilder builder = new BooleanBuilder();

    if (cursor != null) {
      builder.and(indexInfo.id.gt(cursor));
    }

    if (indexClassification != null && !indexClassification.isBlank()) {
      builder.and(indexInfo.indexClassification.containsIgnoreCase(indexClassification));
    }

    if (indexName != null && !indexName.isBlank()) {
      builder.and(indexInfo.indexName.containsIgnoreCase(indexName));
    }

    if (favorite != null) {
      builder.and(indexInfo.favorite.eq(favorite));
    }

    // 동적 정렬
    PathBuilder<IndexInfo> entityPath = new PathBuilder<>(IndexInfo.class, "indexInfo");
    OrderSpecifier<?> orderSpecifier = new OrderSpecifier<>(
        sortDirection != null && sortDirection.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC,
        entityPath.get(sortField != null ? sortField : "id", Comparable.class)

    );

      return queryFactory
          .selectFrom(indexInfo)
          .where(builder)
          .orderBy(orderSpecifier)
          .limit(size)
          .fetch();
    }
}
