package com.zodiakk.timetrackservice.timeentry.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryAuditCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryAuditUpdateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryAuditResponseDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntryAudit;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface TimeEntryAuditMapper {

    TimeEntryAudit toEntity(TimeEntryAuditCreateRequestDto dto);

    TimeEntryAuditResponseDto toResponseDto(TimeEntryAudit entity);

    void updateEntity(TimeEntryAuditUpdateRequestDto dto, @MappingTarget TimeEntryAudit entity);
}
