package com.example.findex.domain.Sync_Job_Log.mapper;

import com.example.findex.domain.Sync_Job_Log.dto.SyncJobLogDto;
import com.example.findex.domain.Sync_Job_Log.entity.SyncJobLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SyncJobLogMapper {

    SyncJobLogMapper INSTANCE = Mappers.getMapper(SyncJobLogMapper.class);

    // SyncJobLog 엔티티 -> SyncJobDto
    @Mapping(source = "indexInfo.id", target = "indexInfoId")
    @Mapping(source = "createdAt", target = "jobTime")
    SyncJobLogDto toDto(SyncJobLog entity);

    List<SyncJobLogDto> toDtoList(List<SyncJobLog> entities);
}
