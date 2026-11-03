package com.zodiakk.timetrackservice.schedule.service;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.WorkScheduleResponseDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface WorkScheduleService {

    WorkScheduleResponseDto create(WorkScheduleCreateRequestDto request);

    WorkScheduleResponseDto update(UUID id, WorkScheduleUpdateRequestDto request);

    WorkScheduleResponseDto get(UUID id);

    Page<WorkScheduleResponseDto> findAll(WorkScheduleFilterDto filter, PageRequestDto pageRequest);
}
