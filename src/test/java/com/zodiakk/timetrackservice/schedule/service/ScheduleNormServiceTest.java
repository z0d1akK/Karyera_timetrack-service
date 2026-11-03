package com.zodiakk.timetrackservice.schedule.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleDayRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleNormServiceTest {

    @Mock
    private EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    @Mock
    private WorkScheduleRepository workScheduleRepository;

    @Mock
    private WorkScheduleDayRepository workScheduleDayRepository;

    @InjectMocks
    private ScheduleNormService service;

    @Test
    void calculateUsesAssignedScheduleAndWarnsWhenMissing() {
        UUID employeeId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        LocalDate monday = LocalDate.of(2026, 10, 19);
        LocalDate tuesday = monday.plusDays(1);

        EmployeeWorkSchedule assignment = new EmployeeWorkSchedule();
        assignment.setScheduleId(scheduleId);
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(scheduleId);
        schedule.setName("STANDARD_40");
        WorkScheduleDay mondayPlan = new WorkScheduleDay();
        mondayPlan.setPlannedMinutes(480);

        when(employeeWorkScheduleRepository.findActiveOnDate(employeeId, monday)).thenReturn(List.of(assignment));
        when(employeeWorkScheduleRepository.findActiveOnDate(employeeId, tuesday)).thenReturn(List.of());
        when(workScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
        when(workScheduleDayRepository.findByScheduleIdAndDayOfWeek(scheduleId, (short) 1))
                .thenReturn(Optional.of(mondayPlan));

        ScheduleNormService.NormCalculation result = service.calculate(employeeId, monday, tuesday);

        assertEquals(480, result.plannedMinutes());
        assertEquals(2, result.days().size());
        assertTrue(result.warnings().getFirst().contains(tuesday.toString()));
    }

    @Test
    void calculateRejectsInvertedRange() {
        LocalDate start = LocalDate.of(2026, 10, 20);
        assertThrows(ConflictException.class, () -> service.calculate(UUID.randomUUID(), start, start.minusDays(1)));
    }
}
