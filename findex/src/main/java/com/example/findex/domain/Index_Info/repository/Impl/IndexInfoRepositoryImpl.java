package com.example.findex.domain.Index_Info.repository.Impl;

import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_Info.repository.IndexInfoRepositoryCustom;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryImpl implements IndexInfoRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<IndexInfo> findByCursorAndFilter(
            String cursor, int size,
            String sortField, String sortDirection,
            String indexClassification, String indexName, Boolean favorite
    ) {
        PathBuilder<IndexInfo> root = new PathBuilder<>(IndexInfo.class, "indexInfo");
        ComparablePath<String> sf = root.getComparable(sortField, String.class);
        NumberPath<Long> id = root.getNumber("id", Long.class);

        BooleanBuilder where = new BooleanBuilder();
        if (indexClassification != null && !indexClassification.isBlank())
            where.and(root.getString("indexClassification")
                    .containsIgnoreCase(indexClassification));
        if (indexName != null && !indexName.isBlank())
            where.and(root.getString("indexName")
                    .containsIgnoreCase(indexName));
        if (favorite != null)
            where.and(root.getBoolean("favorite").eq(favorite));

        if (cursor != null) {
            DecodedCursor decoded = decodeCursor(cursor);
            ComparablePath<String> cursorField = root.getComparable(decoded.sortField, String.class);

            if ("asc".equalsIgnoreCase(sortDirection)) {
                where.and(cursorField.gt(decoded.sortValue)
                        .or(cursorField.eq(decoded.sortValue).and(id.gt(decoded.id))));
            } else {
                where.and(cursorField.lt(decoded.sortValue)
                        .or(cursorField.eq(decoded.sortValue).and(id.lt(decoded.id))));
            }
        }

        Order order = "asc".equalsIgnoreCase(sortDirection) ? Order.ASC : Order.DESC;

        return queryFactory.selectFrom(root)
                .where(where)
                .orderBy(new OrderSpecifier<>(order, sf), new OrderSpecifier<>(order, id))
                .limit(size + 1)
                .fetch();
    }

    private DecodedCursor decodeCursor(String encoded) {
        try {
            String json = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            JsonNode node = objectMapper.readTree(json);
            Long id = node.get("id").asLong();

            String sortField = null;
            String sortValue = null;
            Iterator<String> it = node.fieldNames();
            while (it.hasNext()) {
                String f = it.next();
                if (!f.equals("id")) {
                    sortField = f;
                    sortValue = node.get(f).asText();
                    break;
                }
            }
            return new DecodedCursor(sortField, sortValue, id);
        } catch (Exception e) {
            throw new RuntimeException("Invalid cursor format", e);
        }
    }

    private record DecodedCursor(String sortField, String sortValue, Long id) {}
}
