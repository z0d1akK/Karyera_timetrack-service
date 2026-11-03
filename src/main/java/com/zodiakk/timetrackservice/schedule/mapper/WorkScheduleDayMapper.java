package com.zodiakk.timetrackservice.schedule.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleDayCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleDayUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleDayResponseDto;
import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface WorkScheduleDayMapper {

    WorkScheduleDay toEntity(WorkScheduleDayCreateRequestDto dto);

    WorkScheduleDayResponseDto toResponseDto(WorkScheduleDay entity);

    void updateEntity(WorkScheduleDayUpdateRequestDto dto, @MappingTarget WorkScheduleDay entity);
}
