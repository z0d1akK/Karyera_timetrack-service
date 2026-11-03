package com.zodiakk.timetrackservice.timesheet.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.timesheet.dto.request.create.TimesheetCreateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.update.TimesheetUpdateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetResponseDto;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface TimesheetMapper {

    Timesheet toEntity(TimesheetCreateRequestDto dto);

    TimesheetResponseDto toResponseDto(Timesheet entity);

    void updateEntity(TimesheetUpdateRequestDto dto, @MappingTarget Timesheet entity);
}
