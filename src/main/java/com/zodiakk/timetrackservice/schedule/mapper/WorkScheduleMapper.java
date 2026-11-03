package com.zodiakk.timetrackservice.schedule.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface WorkScheduleMapper {

    WorkSchedule toEntity(WorkScheduleCreateRequestDto dto);

    WorkScheduleResponseDto toResponseDto(WorkSchedule entity);

    void updateEntity(WorkScheduleUpdateRequestDto dto, @MappingTarget WorkSchedule entity);
}
