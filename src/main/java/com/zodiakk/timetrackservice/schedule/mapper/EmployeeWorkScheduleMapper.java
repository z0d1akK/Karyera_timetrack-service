package com.zodiakk.timetrackservice.schedule.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.schedule.dto.request.create.EmployeeWorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.EmployeeWorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.EmployeeWorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface EmployeeWorkScheduleMapper {

    EmployeeWorkSchedule toEntity(EmployeeWorkScheduleCreateRequestDto dto);

    EmployeeWorkScheduleResponseDto toResponseDto(EmployeeWorkSchedule entity);

    void updateEntity(EmployeeWorkScheduleUpdateRequestDto dto, @MappingTarget EmployeeWorkSchedule entity);
}
