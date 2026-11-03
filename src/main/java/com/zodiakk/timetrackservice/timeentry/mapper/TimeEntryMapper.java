package com.zodiakk.timetrackservice.timeentry.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryUpdateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface TimeEntryMapper {

    TimeEntry toEntity(TimeEntryCreateRequestDto dto);

    TimeEntryResponseDto toResponseDto(TimeEntry entity);

    void updateEntity(TimeEntryUpdateRequestDto dto, @MappingTarget TimeEntry entity);
}
