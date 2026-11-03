package com.zodiakk.timetrackservice.internal.service;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.time.Periods;
import com.zodiakk.timetrackservice.internal.dto.ApprovedProjectMinutesDto;
import com.zodiakk.timetrackservice.internal.dto.ApprovedSummaryDto;
import com.zodiakk.timetrackservice.internal.dto.ApprovedTimeEntryDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntryPageableFactory;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntrySpecification;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalTimeService {

    private final TimeEntryRepository timeEntryRepository;

    private final TimesheetRepository timesheetRepository;

    @Transactional(readOnly = true)
    public Page<ApprovedTimeEntryDto> approvedEntries(LocalDate from, LocalDate to, PageRequestDto pageRequest) {
        Periods.requireRange(from, to);

        TimeEntryFilterDto filter = TimeEntryFilterDto.builder()
                .status(TimeEntryStatus.APPROVED)
                .workDateFrom(from)
                .workDateTo(to)
                .build();

        Page<TimeEntry> page = timeEntryRepository.findAll(
                TimeEntrySpecification.filter(filter),
                TimeEntryPageableFactory.create(pageRequest)
        );
        Map<UUID, OffsetDateTime> approvedAtByTimesheet = approvedAt(page.getContent());

        return page.map(entry -> new ApprovedTimeEntryDto(
                entry.getId(),
                entry.getEmployeeId(),
                entry.getProjectId(),
                entry.getTaskId(),
                entry.getProjectAssignmentId(),
                entry.getWorkDate(),
                entry.getDurationMinutes(),
                entry.getTimesheetId() == null ? null : approvedAtByTimesheet.get(entry.getTimesheetId())
        ));
    }

    @Transactional(readOnly = true)
    public ApprovedSummaryDto approvedSummary(UUID employeeId, LocalDate from, LocalDate to) {
        Periods.requireRange(from, to);
        Map<UUID, Long> byProject = new HashMap<>();
        long total = 0;

        for (TimeEntry entry : timeEntryRepository.findAllByEmployeeIdAndWorkDateBetween(employeeId, from, to)) {
            if (entry.getStatus() != TimeEntryStatus.APPROVED) {
                continue;
            }

            total += entry.getDurationMinutes();
            byProject.merge(entry.getProjectId(), (long) entry.getDurationMinutes(), Long::sum);
        }

        List<ApprovedProjectMinutesDto> projects = byProject.entrySet().stream()
                .map(entry -> new ApprovedProjectMinutesDto(entry.getKey(), entry.getValue()))
                .toList();

        return new ApprovedSummaryDto(employeeId, from, to, total, projects);
    }

    private Map<UUID, OffsetDateTime> approvedAt(List<TimeEntry> entries) {
        List<UUID> timesheetIds = entries.stream()
                .map(TimeEntry::getTimesheetId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        if (timesheetIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, OffsetDateTime> approvedAt = new HashMap<>();

        for (Timesheet timesheet : timesheetRepository.findAllById(timesheetIds)) {
            approvedAt.put(timesheet.getId(), timesheet.getApprovedAt());
        }

        return approvedAt;
    }
}
