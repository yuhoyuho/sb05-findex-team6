package com.example.findex.domain.Index_data.mapper;

import com.example.findex.common.base.SourceType;
import com.example.findex.domain.Index_data.dto.IndexDataCreateRequest;
import com.example.findex.domain.Index_data.dto.IndexDataDto;
import com.example.findex.domain.Index_data.dto.IndexDataUpdateRequest;
import com.example.findex.domain.Index_data.entity.IndexData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IndexDataMapper {

    SourceType USER_SOURCE_TYPE = SourceType.사용자;

    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    @Mapping(source = "sourceType", target = "sourceType", qualifiedByName = "sourceTypeToString")
    IndexDataDto toDto(IndexData indexData);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "indexInfo", ignore = true)
    @Mapping(target = "sourceType", expression = "java(USER_SOURCE_TYPE)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    IndexData toEntity(IndexDataCreateRequest request);

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
}