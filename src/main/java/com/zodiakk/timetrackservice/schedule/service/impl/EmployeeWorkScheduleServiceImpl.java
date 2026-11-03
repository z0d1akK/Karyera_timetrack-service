package com.zodiakk.timetrackservice.schedule.service.impl;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.integration.employee.service.EmployeeDirectoryService;
import com.zodiakk.timetrackservice.schedule.dto.request.action.EmployeeWorkScheduleAssignRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.response.EmployeeWorkScheduleResponseDto;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.exception.WorkScheduleNotFoundException;
import com.zodiakk.timetrackservice.schedule.mapper.EmployeeWorkScheduleMapper;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.service.EmployeeWorkScheduleService;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeWorkScheduleServiceImpl implements EmployeeWorkScheduleService {

    private final EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    private final WorkScheduleRepository workScheduleRepository;

    private final EmployeeWorkScheduleMapper employeeWorkScheduleMapper;

    private final EmployeeDirectoryService employeeDirectory;

    private final TimetrackAccess timetrackAccess;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    public EmployeeWorkScheduleResponseDto assign(UUID employeeId, EmployeeWorkScheduleAssignRequestDto request) {
        employeeDirectory.requireActive(employeeId);

        if (request.getValidTo() != null && request.getValidTo().isBefore(request.getValidFrom())) {
            throw new ConflictException("Дата окончания графика не может быть раньше даты начала");
        }

        WorkSchedule schedule = workScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new WorkScheduleNotFoundException(request.getScheduleId()));

        if (!Boolean.TRUE.equals(schedule.getActive())) {
            throw new ConflictException("Нельзя назначить неактивный график");
        }

        List<EmployeeWorkSchedule> openAssignments = employeeWorkScheduleRepository
                .findAllByEmployeeIdOrderByValidFromDesc(employeeId)
                .stream()
                .filter(assignment -> assignment.getValidTo() == null)
                .toList();

        for (EmployeeWorkSchedule current : openAssignments) {
            if (!request.getValidFrom().isAfter(current.getValidFrom())) {
                throw new ConflictException("Новый график должен начинаться позже уже действующего");
            }

            current.setValidTo(request.getValidFrom().minusDays(1));
        }

        EmployeeWorkSchedule assignment = EmployeeWorkSchedule.builder()
                .employeeId(employeeId)
                .scheduleId(schedule.getId())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .build();

        try {
            EmployeeWorkSchedule saved = employeeWorkScheduleRepository.saveAndFlush(assignment);
            return toResponse(saved, schedule.getName());
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Периоды назначения графика пересекаются");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<EmployeeWorkScheduleResponseDto> history(UUID employeeId) {
        timetrackAccess.requireCanView(employeeId);

        return employeeWorkScheduleRepository.findAllByEmployeeIdOrderByValidFromDesc(employeeId)
                .stream()
                .map(assignment -> toResponse(assignment, scheduleName(assignment.getScheduleId())))
                .toList();
    }

    private String scheduleName(UUID scheduleId) {
        return workScheduleRepository.findById(scheduleId)
                .map(WorkSchedule::getName)
                .orElse(null);
    }

    private EmployeeWorkScheduleResponseDto toResponse(EmployeeWorkSchedule assignment, String scheduleName) {
        EmployeeWorkScheduleResponseDto response = employeeWorkScheduleMapper.toResponseDto(assignment);
        response.setScheduleName(scheduleName);
        return response;
    }
}
