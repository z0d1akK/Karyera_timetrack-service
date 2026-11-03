package com.zodiakk.timetrackservice.workload.service;

import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.time.Periods;
import com.zodiakk.timetrackservice.schedule.service.ScheduleNormService;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.workload.dto.DailySummaryDayDto;
import com.zodiakk.timetrackservice.workload.dto.DailySummaryResponseDto;
import com.zodiakk.timetrackservice.workload.dto.EmployeeWorkloadResponseDto;
import com.zodiakk.timetrackservice.workload.dto.ProjectEmployeeMinutesDto;
import com.zodiakk.timetrackservice.workload.dto.ProjectMinutesDto;
import com.zodiakk.timetrackservice.workload.dto.ProjectTimeSummaryResponseDto;
import com.zodiakk.timetrackservice.workload.dto.TeamWorkloadResponseDto;
import com.zodiakk.timetrackservice.workload.dto.WorkCalendarDayDto;
import com.zodiakk.timetrackservice.workload.dto.WorkCalendarResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkloadService {

    private final TimeEntryRepository timeEntryRepository;

    private final ScheduleNormService scheduleNormService;

    private final TimetrackAccess timetrackAccess;

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public DailySummaryResponseDto dailySummary(UUID employeeId, LocalDate from, LocalDate to) {
        timetrackAccess.requireCanView(employeeId);
        Periods.requireRange(from, to);
        List<TimeEntry> entries = timeEntryRepository.findAllByEmployeeIdAndWorkDateBetween(employeeId, from, to);
        List<DailySummaryDayDto> days = new ArrayList<>();

        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            LocalDate current = date;
            long draft = 0;
            long submitted = 0;
            long approved = 0;
            long rejected = 0;

            for (TimeEntry entry : entries) {
                if (!current.equals(entry.getWorkDate()) || entry.getStatus() == TimeEntryStatus.VOIDED) {
                    continue;
                }

                int minutes = entry.getDurationMinutes();

                switch (entry.getStatus()) {
                    case DRAFT -> draft += minutes;
                    case SUBMITTED -> submitted += minutes;
                    case APPROVED -> approved += minutes;
                    case REJECTED -> rejected += minutes;
                    case VOIDED -> {
                    }
                }
            }

            days.add(DailySummaryDayDto.builder()
                    .date(current)
                    .draftMinutes(draft)
                    .submittedMinutes(submitted)
                    .approvedMinutes(approved)
                    .rejectedMinutes(rejected)
                    .totalMinutes(draft + submitted + approved + rejected)
                    .build());
        }

        return DailySummaryResponseDto.builder()
                .employeeId(employeeId)
                .periodStart(from)
                .periodEnd(to)
                .days(days)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public EmployeeWorkloadResponseDto weeklySummary(UUID employeeId, LocalDate weekStart) {
        timetrackAccess.requireCanView(employeeId);
        LocalDate weekEnd = weekStart == null ? null : weekStart.plusDays(6);
        Periods.requireWeek(weekStart, weekEnd);
        return workload(employeeId, weekStart, weekEnd);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public EmployeeWorkloadResponseDto workload(UUID employeeId, LocalDate periodStart, LocalDate periodEnd) {
        timetrackAccess.requireCanView(employeeId);
        Periods.requireRange(periodStart, periodEnd);
        return buildWorkload(employeeId, periodStart, periodEnd);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public WorkCalendarResponseDto workCalendar(UUID employeeId, LocalDate from, LocalDate to) {
        timetrackAccess.requireCanView(employeeId);
        Periods.requireRange(from, to);
        ScheduleNormService.NormCalculation norm = scheduleNormService.calculate(employeeId, from, to);

        List<WorkCalendarDayDto> days = norm.days().stream()
                .map(day -> WorkCalendarDayDto.builder()
                        .date(day.date())
                        .plannedMinutes(day.plannedMinutes())
                        .scheduleId(day.scheduleId())
                        .scheduleName(day.scheduleName())
                        .build())
                .toList();

        return WorkCalendarResponseDto.builder()
                .employeeId(employeeId)
                .periodStart(from)
                .periodEnd(to)
                .plannedMinutes(norm.plannedMinutes())
                .days(days)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public ProjectTimeSummaryResponseDto projectSummary(UUID projectId, LocalDate periodStart, LocalDate periodEnd) {
        timetrackAccess.requireCanViewProjectSummary();
        Periods.requireRange(periodStart, periodEnd);

        Map<UUID, long[]> byEmployee = new LinkedHashMap<>();
        long approved = 0;
        long pending = 0;
        long draft = 0;

        for (TimeEntry entry : timeEntryRepository.findAllByProjectIdAndWorkDateBetween(projectId, periodStart, periodEnd)) {
            if (entry.getStatus() == TimeEntryStatus.VOIDED) {
                continue;
            }

            long[] buckets = byEmployee.computeIfAbsent(entry.getEmployeeId(), ignored -> new long[3]);
            int minutes = entry.getDurationMinutes();

            switch (entry.getStatus()) {
                case APPROVED -> {
                    buckets[0] += minutes;
                    approved += minutes;
                }
                case SUBMITTED -> {
                    buckets[1] += minutes;
                    pending += minutes;
                }
                case DRAFT, REJECTED -> {
                    buckets[2] += minutes;
                    draft += minutes;
                }
                case VOIDED -> {
                }
            }
        }

        List<ProjectEmployeeMinutesDto> employees = byEmployee.entrySet().stream()
                .map(entry -> ProjectEmployeeMinutesDto.builder()
                        .employeeId(entry.getKey())
                        .approvedMinutes(entry.getValue()[0])
                        .pendingMinutes(entry.getValue()[1])
                        .draftMinutes(entry.getValue()[2])
                        .build())
                .sorted(Comparator.comparing(ProjectEmployeeMinutesDto::getEmployeeId))
                .toList();

        return ProjectTimeSummaryResponseDto.builder()
                .projectId(projectId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .approvedMinutes(approved)
                .pendingMinutes(pending)
                .draftMinutes(draft)
                .employees(employees)
                .build();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TeamWorkloadResponseDto teamWorkload(LocalDate periodStart, LocalDate periodEnd) {
        timetrackAccess.requireCanReviewQueue();
        Periods.requireRange(periodStart, periodEnd);

        List<UUID> employeeIds;

        if (timetrackAccess.isHrOrAdmin()) {
            employeeIds = timeEntryRepository.findDistinctEmployeeIdsByWorkDateBetween(periodStart, periodEnd);
        } else {
            UUID self = timetrackAccess.currentEmployeeId();
            employeeIds = timetrackAccess.visibleEmployeeIds().stream()
                    .filter(employeeId -> !employeeId.equals(self))
                    .toList();
        }

        List<EmployeeWorkloadResponseDto> employees = employeeIds.stream()
                .map(employeeId -> buildWorkload(employeeId, periodStart, periodEnd))
                .toList();

        return TeamWorkloadResponseDto.builder()
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .employees(employees)
                .build();
    }

    private EmployeeWorkloadResponseDto buildWorkload(UUID employeeId, LocalDate periodStart, LocalDate periodEnd) {
        List<TimeEntry> entries = timeEntryRepository.findAllByEmployeeIdAndWorkDateBetween(
                employeeId,
                periodStart,
                periodEnd
        );
        ScheduleNormService.NormCalculation norm = scheduleNormService.calculate(employeeId, periodStart, periodEnd);
        long approved = 0;
        long pending = 0;
        long draft = 0;
        Map<UUID, Long> approvedByProject = new LinkedHashMap<>();

        for (TimeEntry entry : entries) {
            if (entry.getStatus() == TimeEntryStatus.VOIDED) {
                continue;
            }

            int minutes = entry.getDurationMinutes();

            switch (entry.getStatus()) {
                case APPROVED -> {
                    approved += minutes;
                    approvedByProject.merge(entry.getProjectId(), (long) minutes, Long::sum);
                }
                case SUBMITTED -> pending += minutes;
                case DRAFT, REJECTED -> draft += minutes;
                case VOIDED -> {
                }
            }
        }

        List<ProjectMinutesDto> projects = approvedByProject.entrySet().stream()
                .map(entry -> ProjectMinutesDto.builder()
                        .projectId(entry.getKey())
                        .approvedMinutes(entry.getValue())
                        .build())
                .toList();

        return EmployeeWorkloadResponseDto.builder()
                .employeeId(employeeId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .plannedMinutes(norm.plannedMinutes())
                .approvedMinutes(approved)
                .pendingMinutes(pending)
                .draftMinutes(draft)
                .projects(projects)
                .build();
    }
}
