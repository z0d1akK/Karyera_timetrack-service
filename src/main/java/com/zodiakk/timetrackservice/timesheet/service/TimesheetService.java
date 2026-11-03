package com.zodiakk.timetrackservice.timesheet.service;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetApproveRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetRejectRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetSubmitRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.create.TimesheetCreateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetFilterDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetReviewResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetSubmitResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TimesheetService {

    TimesheetResponseDto create(TimesheetCreateRequestDto request);

    TimesheetResponseDto get(UUID id);

    Page<TimesheetResponseDto> findAll(TimesheetFilterDto filter, PageRequestDto pageRequest);

    List<TimesheetResponseDto> findByEmployee(UUID employeeId);

    TimesheetSubmitResponseDto submit(UUID id, TimesheetSubmitRequestDto request);

    TimesheetResponseDto approve(UUID id, TimesheetApproveRequestDto request);

    TimesheetResponseDto reject(UUID id, TimesheetRejectRequestDto request);

    List<TimesheetReviewResponseDto> reviews(UUID id);

    List<TimesheetResponseDto> pendingApproval();
}
