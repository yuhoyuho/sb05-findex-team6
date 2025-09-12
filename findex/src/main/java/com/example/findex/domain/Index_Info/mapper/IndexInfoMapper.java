package com.example.findex.domain.Index_Info.mapper;

import com.example.findex.domain.Index_Info.dto.IndexInfoCreateRequest;
import com.example.findex.domain.Index_Info.dto.IndexInfoDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoSummaryDto;
import com.example.findex.domain.Index_Info.dto.IndexInfoUpdateDto;
import com.example.findex.domain.Index_Info.entity.IndexInfo;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IndexInfoMapper {

  IndexInfoDto toDto(IndexInfo entity);

  @Mapping(target = "sourceType", expression = "java(com.example.findex.common.base.SourceType.USER)")
  IndexInfo toEntity(IndexInfoCreateRequest request);

  void updateEntityFromRequest(IndexInfoUpdateDto request, @MappingTarget IndexInfo entity);

  List<IndexInfoDto> toDtoList(List<IndexInfo> entities);

  IndexInfoSummaryDto toSummaryDto(IndexInfo entity);

}
