// com/example/findex/domain/Index_data/repository/impl/IndexDataRepositoryImpl.java
package com.example.findex.domain.Index_data.repository.impl;

import com.example.findex.domain.Index_data.entity.IndexData;
import com.example.findex.domain.Index_data.entity.QIndexData;
import com.example.findex.domain.Index_data.repository.IndexDataRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static com.example.findex.domain.Auto_Sync.entity.QAutoSync.autoSync;
import static com.example.findex.domain.Index_Info.entity.QIndexInfo.indexInfo;
import static com.example.findex.domain.Index_data.entity.QIndexData.indexData;

@Repository
@RequiredArgsConstructor
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexData> findDataWithDynamicSort(
            Long indexInfoId, LocalDate startDate, LocalDate endDate,
            String sortField, String sortDirection, String cursor, Long idAfter, Integer size) {

        BooleanBuilder whereClause = new BooleanBuilder();
        if (indexInfoId != null) {
            whereClause.and(indexData.indexInfo.id.eq(indexInfoId));
        }
        if (startDate != null) {
            whereClause.and(indexData.baseDate.goe(startDate));
        }
        if (endDate != null) {
            whereClause.and(indexData.baseDate.loe(endDate));
        }

        BooleanExpression cursorCondition = buildCursorCondition(cursor, idAfter, sortField, sortDirection);
        if (cursorCondition != null) {
            whereClause.and(cursorCondition);
        }

        return queryFactory
                .selectFrom(indexData)
                .join(indexData.indexInfo, indexInfo).fetchJoin()
                .leftJoin(indexInfo.autoSync, autoSync).fetchJoin()
                .where(whereClause)
                .orderBy(buildOrderSpecifier(sortField, sortDirection))
                .limit(size + 1)
                .fetch();
    }

    @Override
    public List<IndexData> findLatestSnapshotInRange(LocalDate start, LocalDate end, Set<Long> indexIds) {
        QIndexData sub = new QIndexData("sub");
        BooleanBuilder where = new BooleanBuilder();
        if (indexIds != null && !indexIds.isEmpty()) {
            where.and(indexData.indexInfo.id.in(indexIds));
        }
        return queryFactory
                .selectFrom(indexData)
                .join(indexData.indexInfo, indexInfo).fetchJoin()
                .where(
                        where,
                        indexData.baseDate.eq(
                                JPAExpressions
                                        .select(sub.baseDate.max())
                                        .from(sub)
                                        .where(
                                                sub.indexInfo.id.eq(indexData.indexInfo.id)
                                                        .and(sub.baseDate.goe(start))
                                                        .and(sub.baseDate.loe(end))
                                                        .and(indexIds != null && !indexIds.isEmpty()
                                                                ? sub.indexInfo.id.in(indexIds)
                                                                : null)
                                        )
                        )
                )
                .fetch();
    }

    @Override
    public List<IndexData> findEarliestSnapshotInRange(LocalDate start, LocalDate end, Set<Long> indexIds) {
        QIndexData sub = new QIndexData("sub");
        BooleanBuilder where = new BooleanBuilder();
        if (indexIds != null && !indexIds.isEmpty()) {
            where.and(indexData.indexInfo.id.in(indexIds));
        }
        return queryFactory
                .selectFrom(indexData)
                .join(indexData.indexInfo, indexInfo).fetchJoin()
                .where(
                        where,
                        indexData.baseDate.eq(
                                JPAExpressions
                                        .select(sub.baseDate.min())
                                        .from(sub)
                                        .where(
                                                sub.indexInfo.id.eq(indexData.indexInfo.id)
                                                        .and(sub.baseDate.goe(start))
                                                        .and(sub.baseDate.loe(end))
                                                        .and(indexIds != null && !indexIds.isEmpty()
                                                                ? sub.indexInfo.id.in(indexIds)
                                                                : null)
                                        )
                        )
                )
                .fetch();
    }

    @Override
    public LocalDate findLatestBaseDate() {
        return queryFactory
                .select(indexData.baseDate.max())
                .from(indexData)
                .fetchOne();
    }

    private BooleanExpression buildCursorCondition(String cursor, Long idAfter, String sortField, String sortDirection) {
        if (cursor == null && idAfter == null) return null;

        boolean isDesc = "desc".equalsIgnoreCase(sortDirection);
        String validSortField = getValidSortField(sortField);

        Object cursorValue = extractCursorValue(cursor, idAfter, validSortField);
        Long cursorId = extractCursorId(cursor, idAfter);

        if (cursorValue == null) {
            return isDesc ? indexData.id.lt(cursorId) : indexData.id.gt(cursorId);
        }

        return switch (validSortField) {
            case "baseDate" -> buildDateCursorCondition((LocalDate) cursorValue, cursorId, isDesc);
            case "closingPrice" -> buildPriceCursorCondition(indexData.closingPrice, (BigDecimal) cursorValue, cursorId, isDesc);
            case "marketPrice" -> buildPriceCursorCondition(indexData.marketPrice, (BigDecimal) cursorValue, cursorId, isDesc);
            case "highPrice" -> buildPriceCursorCondition(indexData.highPrice, (BigDecimal) cursorValue, cursorId, isDesc);
            case "lowPrice" -> buildPriceCursorCondition(indexData.lowPrice, (BigDecimal) cursorValue, cursorId, isDesc);
            case "tradingQuantity" -> buildLongCursorCondition(indexData.tradingQuantity, (Long) cursorValue, cursorId, isDesc);
            case "tradingPrice" -> buildLongCursorCondition(indexData.tradingPrice, (Long) cursorValue, cursorId, isDesc);
            case "marketTotalAmount" -> buildLongCursorCondition(indexData.marketTotalAmount, (Long) cursorValue, cursorId, isDesc);
            default -> isDesc ? indexData.id.lt(cursorId) : indexData.id.gt(cursorId);
        };
    }

    private BooleanExpression buildDateCursorCondition(LocalDate cursorDate, Long cursorId, boolean isDesc) {
        if (isDesc) {
            return indexData.baseDate.lt(cursorDate)
                    .or(indexData.baseDate.eq(cursorDate).and(indexData.id.lt(cursorId)));
        } else {
            return indexData.baseDate.gt(cursorDate)
                    .or(indexData.baseDate.eq(cursorDate).and(indexData.id.gt(cursorId)));
        }
    }

    private BooleanExpression buildPriceCursorCondition(
            NumberPath<BigDecimal> priceField,
            BigDecimal cursorPrice, Long cursorId, boolean isDesc) {

        if (isDesc) {
            return priceField.lt(cursorPrice)
                    .or(priceField.eq(cursorPrice).and(indexData.id.lt(cursorId)));
        } else {
            return priceField.gt(cursorPrice)
                    .or(priceField.eq(cursorPrice).and(indexData.id.gt(cursorId)));
        }
    }

    private BooleanExpression buildLongCursorCondition(
            NumberPath<Long> longField,
            Long cursorValue, Long cursorId, boolean isDesc) {

        if (isDesc) {
            return longField.lt(cursorValue)
                    .or(longField.eq(cursorValue).and(indexData.id.lt(cursorId)));
        } else {
            return longField.gt(cursorValue)
                    .or(longField.eq(cursorValue).and(indexData.id.gt(cursorId)));
        }
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(String sortField, String sortDirection) {
        boolean isDesc = "desc".equalsIgnoreCase(sortDirection);
        String validSortField = getValidSortField(sortField);

        OrderSpecifier<?> primaryOrder = switch (validSortField) {
            case "baseDate" -> isDesc ? indexData.baseDate.desc() : indexData.baseDate.asc();
            case "closingPrice" -> isDesc ? indexData.closingPrice.desc() : indexData.closingPrice.asc();
            case "marketPrice" -> isDesc ? indexData.marketPrice.desc() : indexData.marketPrice.asc();
            case "highPrice" -> isDesc ? indexData.highPrice.desc() : indexData.highPrice.asc();
            case "lowPrice" -> isDesc ? indexData.lowPrice.desc() : indexData.lowPrice.asc();
            case "tradingQuantity" -> isDesc ? indexData.tradingQuantity.desc() : indexData.tradingQuantity.asc();
            case "tradingPrice" -> isDesc ? indexData.tradingPrice.desc() : indexData.tradingPrice.asc();
            case "marketTotalAmount" -> isDesc ? indexData.marketTotalAmount.desc() : indexData.marketTotalAmount.asc();
            default -> indexData.baseDate.desc();
        };

        OrderSpecifier<?> secondaryOrder = indexData.id.desc();
        return new OrderSpecifier[]{primaryOrder, secondaryOrder};
    }

    private Object extractCursorValue(String cursor, Long idAfter, String sortField) {
        if (cursor != null) {
            try {
                String decodedCursor = new String(Base64.getDecoder().decode(cursor));
                return parseCursorValue(decodedCursor, sortField);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private Long extractCursorId(String cursor, Long idAfter) {
        if (cursor != null) {
            try {
                String decodedCursor = new String(Base64.getDecoder().decode(cursor));
                if (decodedCursor.contains("\"id\":")) {
                    String idPart = decodedCursor.substring(decodedCursor.indexOf("\"id\":") + 5);
                    String idStr = idPart.replaceAll("[^0-9]", "");
                    if (!idStr.isEmpty()) {
                        return Long.parseLong(idStr);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return idAfter != null ? idAfter : 0L;
    }

    private Object parseCursorValue(String decodedCursor, String sortField) {
        return switch (getValidSortField(sortField)) {
            case "baseDate" -> {
                String dateStr = decodedCursor.replaceAll("[^0-9\\-]", "");
                yield dateStr.length() >= 10 ? LocalDate.parse(dateStr.substring(0, 10)) : null;
            }
            case "closingPrice", "marketPrice", "highPrice", "lowPrice" -> {
                String numStr = decodedCursor.replaceAll("[^0-9.]", "");
                yield !numStr.isEmpty() ? new BigDecimal(numStr) : null;
            }
            case "tradingQuantity", "tradingPrice", "marketTotalAmount" -> {
                String numStr = decodedCursor.replaceAll("[^0-9]", "");
                yield !numStr.isEmpty() ? Long.parseLong(numStr) : null;
            }
            default -> null;
        };
    }

    private String getValidSortField(String sortField) {
        if (sortField == null) return "baseDate";
        return switch (sortField) {
            case "baseDate", "closingPrice", "marketPrice", "highPrice", "lowPrice",
                 "tradingQuantity", "tradingPrice", "marketTotalAmount" -> sortField;
            default -> "baseDate";
        };
    }
}
