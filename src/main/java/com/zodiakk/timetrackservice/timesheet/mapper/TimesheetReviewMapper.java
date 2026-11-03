package com.zodiakk.timetrackservice.timesheet.mapper;

import com.zodiakk.timetrackservice.config.MapStructConfig;
import com.zodiakk.timetrackservice.timesheet.dto.request.create.TimesheetReviewCreateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.update.TimesheetReviewUpdateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetReviewResponseDto;
import com.zodiakk.timetrackservice.timesheet.entity.TimesheetReview;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface TimesheetReviewMapper {

    TimesheetReview toEntity(TimesheetReviewCreateRequestDto dto);

    TimesheetReviewResponseDto toResponseDto(TimesheetReview entity);

    void updateEntity(TimesheetReviewUpdateRequestDto dto, @MappingTarget TimesheetReview entity);
}
