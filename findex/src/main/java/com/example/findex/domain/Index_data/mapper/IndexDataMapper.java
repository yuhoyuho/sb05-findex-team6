package com.example.findex.domain.Index_data.mapper;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.entity.IndexData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

    SourceType USER_SOURCE_TYPE = SourceType.USER;

    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    @Mapping(source = "sourceType", target = "sourceType", qualifiedByName = "sourceTypeToString")
    @Mapping(source = "marketPrice", target = "marketPrice", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "closingPrice", target = "closingPrice", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "highPrice", target = "highPrice", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "lowPrice", target = "lowPrice", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "versus", target = "versus", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "fluctuationRate", target = "fluctuationRate", qualifiedByName = "nullToZeroBigDecimal")
    @Mapping(source = "tradingQuantity", target = "tradingQuantity", qualifiedByName = "nullToZeroLong")
    @Mapping(source = "tradingPrice", target = "tradingPrice", qualifiedByName = "nullToZeroLong")
    @Mapping(source = "marketTotalAmount", target = "marketTotalAmount", qualifiedByName = "nullToZeroLong")
    IndexDataDto toDto(IndexData indexData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexInfo", source = "indexInfo")
    @Mapping(target = "sourceType", expression = "java(USER_SOURCE_TYPE)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    IndexData toEntity(IndexDataCreateRequest request, IndexInfo indexInfo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexInfo", ignore = true)
    @Mapping(target = "baseDate", ignore = true)
    @Mapping(target = "sourceType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(IndexDataUpdateRequest request, @MappingTarget IndexData indexData);

    List<IndexDataDto> toDtoList(List<IndexData> indexDataList);

    @Named("sourceTypeToString")
    default String sourceTypeToString(SourceType sourceType) {
        return sourceType != null ? sourceType.name() : null;
    }

    @Named("nullToZeroBigDecimal")
    default BigDecimal nullToZeroBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Named("nullToZeroLong")
    default Long nullToZeroLong(Long value) {
        return value != null ? value : 0L;
    }
}