package com.zodiakk.timetrackservice.schedule.service.impl;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleCreateRequestDto;
import com.zodiakk.timetrackservice.schedule.dto.request.create.WorkScheduleDayInputDto;
import com.zodiakk.timetrackservice.schedule.dto.request.update.WorkScheduleUpdateRequestDto;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.exception.WorkScheduleNotFoundException;
import com.zodiakk.timetrackservice.schedule.mapper.WorkScheduleDayMapper;
import com.zodiakk.timetrackservice.schedule.mapper.WorkScheduleMapper;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleDayRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkScheduleServiceImplTest {

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private WorkScheduleDayRepository workScheduleDayRepository;

    @Mock
    private EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    @Mock
    private WorkScheduleMapper workScheduleMapper;

    @Mock
    private WorkScheduleDayMapper workScheduleDayMapper;

    @InjectMocks
    private WorkScheduleServiceImpl service;

    @Test
    void createRejectsDuplicateCode() {
        WorkScheduleCreateRequestDto request = new WorkScheduleCreateRequestDto();
        request.setCode("STANDARD_40");
        request.setTimezone("Europe/Vilnius");
        when(workScheduleRepository.existsByCodeIgnoreCase("STANDARD_40")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(request));
        verify(workScheduleRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createRejectsUnknownTimezone() {
        WorkScheduleCreateRequestDto request = new WorkScheduleCreateRequestDto();
        request.setCode("FLEX");
        request.setTimezone("Not/AZone");

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void createRejectsRepeatedDay() {
        WorkScheduleDayInputDto monday = day((short) 1);
        WorkScheduleCreateRequestDto request = new WorkScheduleCreateRequestDto();
        request.setCode("FLEX");
        request.setTimezone("Europe/Vilnius");
        request.setDays(List.of(monday, monday));

        assertThrows(ConflictException.class, () -> service.create(request));
    }

    @Test
    void updateRejectsStructuralChangeWhenAssigned() {
        UUID id = UUID.randomUUID();
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(id);
        when(workScheduleRepository.findByIdForUpdate(id)).thenReturn(Optional.of(schedule));
        when(employeeWorkScheduleRepository.findAllByScheduleId(id)).thenReturn(List.of(new EmployeeWorkSchedule()));

        WorkScheduleUpdateRequestDto request = new WorkScheduleUpdateRequestDto();
        request.setDays(List.of(day((short) 1)));

        assertThrows(ConflictException.class, () -> service.update(id, request));
    }

    @Test
    void getThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(workScheduleRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WorkScheduleNotFoundException.class, () -> service.get(id));
    }

    private WorkScheduleDayInputDto day(short dayOfWeek) {
        WorkScheduleDayInputDto day = new WorkScheduleDayInputDto();
        day.setDayOfWeek(dayOfWeek);
        day.setPlannedMinutes(480);
        return day;
    }
}
