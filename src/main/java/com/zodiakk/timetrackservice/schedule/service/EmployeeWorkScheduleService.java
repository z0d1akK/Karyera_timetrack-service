package com.zodiakk.timetrackservice.schedule.service;

import com.zodiakk.timetrackservice.schedule.dto.request.action.EmployeeWorkScheduleAssignRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.EmployeeWorkScheduleResponseDto;

import java.util.List;
import java.util.UUID;

public interface EmployeeWorkScheduleService {

    EmployeeWorkScheduleResponseDto assign(UUID employeeId, EmployeeWorkScheduleAssignRequestDto request);

    List<EmployeeWorkScheduleResponseDto> history(UUID employeeId);
}
