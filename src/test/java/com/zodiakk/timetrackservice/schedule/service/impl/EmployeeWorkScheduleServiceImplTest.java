package com.zodiakk.timetrackservice.schedule.service.impl;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.employee.service.EmployeeDirectoryService;
import com.zodiakk.timetrackservice.schedule.dto.request.action.EmployeeWorkScheduleAssignRequestDto;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.exception.WorkScheduleNotFoundException;
import com.zodiakk.timetrackservice.schedule.mapper.EmployeeWorkScheduleMapper;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeWorkScheduleServiceImplTest {

    @Mock
    private EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private EmployeeWorkScheduleMapper employeeWorkScheduleMapper;

    @Mock
    private EmployeeDirectoryService employeeDirectory;

    @Mock
    private TimetrackAccess timetrackAccess;

    @InjectMocks
    private EmployeeWorkScheduleServiceImpl service;

    @Test
    void assignRejectsInvertedPeriod() {
        UUID employeeId = UUID.randomUUID();
        when(employeeDirectory.requireActive(employeeId)).thenReturn(employee());
        EmployeeWorkScheduleAssignRequestDto request = request(LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 1));

        assertThrows(ConflictException.class, () -> service.assign(employeeId, request));
    }

    @Test
    void assignRejectsMissingSchedule() {
        UUID employeeId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(employeeDirectory.requireActive(employeeId)).thenReturn(employee());
        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        EmployeeWorkScheduleAssignRequestDto request = request(LocalDate.of(2026, 11, 2), null);
        request.setScheduleId(scheduleId);

        assertThrows(WorkScheduleNotFoundException.class, () -> service.assign(employeeId, request));
    }

    @Test
    void assignRejectsInactiveSchedule() {
        UUID employeeId = UUID.randomUUID();
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(UUID.randomUUID());
        schedule.setActive(false);
        when(employeeDirectory.requireActive(employeeId)).thenReturn(employee());
        when(workScheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));

        EmployeeWorkScheduleAssignRequestDto request = request(LocalDate.of(2026, 11, 2), null);
        request.setScheduleId(schedule.getId());

        assertThrows(ConflictException.class, () -> service.assign(employeeId, request));
    }

    @Test
    void assignRejectsOverlappingOpenPeriod() {
        UUID employeeId = UUID.randomUUID();
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(UUID.randomUUID());
        schedule.setActive(true);
        EmployeeWorkSchedule current = new EmployeeWorkSchedule();
        current.setValidFrom(LocalDate.of(2026, 11, 2));
        when(employeeDirectory.requireActive(employeeId)).thenReturn(employee());
        when(workScheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(employeeWorkScheduleRepository.findAllByEmployeeIdOrderByValidFromDesc(employeeId))
                .thenReturn(List.of(current));

        EmployeeWorkScheduleAssignRequestDto request = request(LocalDate.of(2026, 11, 2), null);
        request.setScheduleId(schedule.getId());

        assertThrows(ConflictException.class, () -> service.assign(employeeId, request));
    }

    private EmployeeLookupItem employee() {
        return new EmployeeLookupItem(UUID.randomUUID(), "Ivan Petrov", true, null, null);
    }

    private EmployeeWorkScheduleAssignRequestDto request(LocalDate from, LocalDate to) {
        EmployeeWorkScheduleAssignRequestDto request = new EmployeeWorkScheduleAssignRequestDto();
        request.setScheduleId(UUID.randomUUID());
        request.setValidFrom(from);
        request.setValidTo(to);
        return request;
    }
}
