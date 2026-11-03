package com.zodiakk.timetrackservice.schedule.service;

import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;
import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;
import com.zodiakk.timetrackservice.schedule.exception.WorkScheduleNotFoundException;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleDayRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleNormService {

    private final EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    private final WorkScheduleRepository workScheduleRepository;

    private final WorkScheduleDayRepository workScheduleDayRepository;

    @Transactional(readOnly = true)
    public NormCalculation calculate(UUID employeeId, LocalDate periodStart, LocalDate periodEnd) {
        if (periodEnd.isBefore(periodStart)) {
            throw new ConflictException("Окончание периода не может быть раньше начала");
        }

        int plannedMinutes = 0;
        List<NormDay> days = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (LocalDate date = periodStart; !date.isAfter(periodEnd); date = date.plusDays(1)) {
            List<EmployeeWorkSchedule> assignments = employeeWorkScheduleRepository.findActiveOnDate(employeeId, date);

            if (assignments.isEmpty()) {
                days.add(new NormDay(date, 0, null, null));
                warnings.add("На дату %s сотруднику не назначен рабочий график".formatted(date));
                continue;
            }

            EmployeeWorkSchedule assignment = assignments.getFirst();
            WorkSchedule schedule = workScheduleRepository.findById(assignment.getScheduleId())
                    .orElseThrow(() -> new WorkScheduleNotFoundException(assignment.getScheduleId()));
            short dayOfWeek = (short) date.getDayOfWeek().getValue();
            int minutes = workScheduleDayRepository.findByScheduleIdAndDayOfWeek(schedule.getId(), dayOfWeek)
                    .map(WorkScheduleDay::getPlannedMinutes)
                    .orElse(0);

            plannedMinutes += minutes;
            days.add(new NormDay(date, minutes, schedule.getId(), schedule.getName()));
        }

        return new NormCalculation(plannedMinutes, List.copyOf(days), List.copyOf(warnings));
    }

    public record NormDay(
            LocalDate date,
            int plannedMinutes,
            UUID scheduleId,
            String scheduleName
    ) {
    }

    public record NormCalculation(
            int plannedMinutes,
            List<NormDay> days,
            List<String> warnings
    ) {
    }
}
