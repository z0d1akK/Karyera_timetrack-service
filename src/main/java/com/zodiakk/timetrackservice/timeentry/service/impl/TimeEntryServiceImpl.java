package com.zodiakk.timetrackservice.timeentry.service.impl;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.enums.TimeEntryAuditAction;
import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.enums.TimesheetStatus;
import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import com.zodiakk.timetrackservice.integration.project.service.ProjectDirectoryService;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import com.zodiakk.timetrackservice.timeentry.dto.request.action.TimeEntryVoidRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryUpdateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryAuditResponseDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.exception.TimeEntryNotFoundException;
import com.zodiakk.timetrackservice.timeentry.lock.EmployeeDayLock;
import com.zodiakk.timetrackservice.timeentry.mapper.TimeEntryAuditMapper;
import com.zodiakk.timetrackservice.timeentry.mapper.TimeEntryMapper;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryAuditRepository;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntryPageableFactory;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntrySpecification;
import com.zodiakk.timetrackservice.timeentry.service.TimeEntryAuditWriter;
import com.zodiakk.timetrackservice.timeentry.service.TimeEntryService;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeEntryServiceImpl implements TimeEntryService {

    private static final int MAX_DAILY_MINUTES = 1440;

    private static final Set<TimeEntryStatus> COUNTED_STATUSES = EnumSet.of(
            TimeEntryStatus.DRAFT,
            TimeEntryStatus.REJECTED,
            TimeEntryStatus.SUBMITTED,
            TimeEntryStatus.APPROVED
    );

    private static final Set<TimeEntryStatus> EDITABLE_STATUSES = EnumSet.of(
            TimeEntryStatus.DRAFT,
            TimeEntryStatus.REJECTED
    );

    private static final Set<TimesheetStatus> LOCKED_TIMESHEET_STATUSES = EnumSet.of(
            TimesheetStatus.SUBMITTED,
            TimesheetStatus.APPROVED
    );

    private final TimeEntryRepository timeEntryRepository;

    private final TimeEntryAuditRepository timeEntryAuditRepository;

    private final TimesheetRepository timesheetRepository;

    private final TimeEntryMapper timeEntryMapper;

    private final TimeEntryAuditMapper timeEntryAuditMapper;

    private final TimeEntryAuditWriter timeEntryAuditWriter;

    private final ProjectDirectoryService projectDirectory;

    private final EmployeeDayLock employeeDayLock;

    private final TimetrackAccess timetrackAccess;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimeEntryResponseDto create(TimeEntryCreateRequestDto request) {
        UUID employeeId = timetrackAccess.currentEmployeeId();
        requireDuration(request.getDurationMinutes());
        requireOpenDate(employeeId, request.getWorkDate());

        WorkEligibilityDto eligibility = projectDirectory.requireEligible(
                request.getProjectId(),
                request.getTaskId(),
                employeeId,
                request.getWorkDate()
        );

        requireDailyCapacity(employeeId, request.getWorkDate(), request.getDurationMinutes(), null);

        TimeEntry entry = timeEntryMapper.toEntity(request);
        entry.setEmployeeId(employeeId);
        entry.setCreatedBy(employeeId);
        entry.setStatus(TimeEntryStatus.DRAFT);
        entry.setProjectAssignmentId(eligibility.projectAssignmentId());
        entry.setTimesheetId(null);

        TimeEntry saved = timeEntryRepository.save(entry);
        timeEntryAuditWriter.created(saved, employeeId);
        return timeEntryMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimeEntryResponseDto update(UUID id, TimeEntryUpdateRequestDto request) {
        TimeEntry entry = timeEntryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        requireOwner(entry);

        if (!EDITABLE_STATUSES.contains(entry.getStatus())) {
            throw new ConflictException("Редактировать можно только черновик или отклонённую запись");
        }

        TimeEntry before = copy(entry);
        UUID projectId = request.getProjectId() != null ? request.getProjectId() : entry.getProjectId();
        UUID taskId = request.getTaskId() != null ? request.getTaskId() : entry.getTaskId();
        LocalDate workDate = request.getWorkDate() != null ? request.getWorkDate() : entry.getWorkDate();
        int duration = request.getDurationMinutes() != null ? request.getDurationMinutes() : entry.getDurationMinutes();

        requireDuration(duration);
        requireOpenDate(entry.getEmployeeId(), entry.getWorkDate());
        requireOpenDate(entry.getEmployeeId(), workDate);

        WorkEligibilityDto eligibility = projectDirectory.requireEligible(
                projectId,
                taskId,
                entry.getEmployeeId(),
                workDate
        );

        if (!workDate.equals(entry.getWorkDate())) {
            employeeDayLock.lock(entry.getEmployeeId(), entry.getWorkDate());
        }

        requireDailyCapacity(entry.getEmployeeId(), workDate, duration, entry.getId());

        entry.setProjectId(projectId);
        entry.setTaskId(taskId);
        entry.setWorkDate(workDate);
        entry.setDurationMinutes(duration);
        entry.setProjectAssignmentId(eligibility.projectAssignmentId());

        if (request.getDescription() != null) {
            entry.setDescription(request.getDescription());
        }

        TimeEntry saved = timeEntryRepository.save(entry);
        timeEntryAuditWriter.updated(before, saved, timetrackAccess.currentEmployeeId());
        return timeEntryMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimeEntryResponseDto voidEntry(UUID id, TimeEntryVoidRequestDto request) {
        TimeEntry entry = timeEntryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        requireOwner(entry);
        requireVersion(entry.getVersion(), request.getExpectedVersion());

        if (!EDITABLE_STATUSES.contains(entry.getStatus())) {
            throw new ConflictException("Аннулировать можно только черновик или отклонённую запись");
        }

        requireOpenDate(entry.getEmployeeId(), entry.getWorkDate());
        employeeDayLock.lock(entry.getEmployeeId(), entry.getWorkDate());

        TimeEntry before = copy(entry);
        entry.setStatus(TimeEntryStatus.VOIDED);
        TimeEntry saved = timeEntryRepository.save(entry);

        Map<String, Object> changes = timeEntryAuditWriter.diff(before, saved);
        changes.put("reason", Map.of("new", request.getReason()));
        timeEntryAuditWriter.action(saved, timetrackAccess.currentEmployeeId(), TimeEntryAuditAction.VOIDED, changes);
        return timeEntryMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TimeEntryResponseDto get(UUID id) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        timetrackAccess.requireCanView(entry.getEmployeeId());
        return timeEntryMapper.toResponseDto(entry);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<TimeEntryResponseDto> findAll(TimeEntryFilterDto filter, PageRequestDto pageRequest) {
        TimeEntryFilterDto safeFilter = filter == null ? new TimeEntryFilterDto() : filter;
        Specification<TimeEntry> specification = TimeEntrySpecification.filter(safeFilter);
        Set<UUID> visible = timetrackAccess.visibleEmployeeIds();

        if (visible != null) {
            if (safeFilter.getEmployeeId() != null && !visible.contains(safeFilter.getEmployeeId())) {
                throw new AccessDeniedException("Нет доступа к записям выбранного сотрудника");
            }

            specification = specification.and((root, query, criteriaBuilder) -> root.get("employeeId").in(visible));
        }

        return timeEntryRepository.findAll(specification, TimeEntryPageableFactory.create(pageRequest))
                .map(timeEntryMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TimeEntryAuditResponseDto> audit(UUID id) {
        TimeEntry entry = timeEntryRepository.findById(id)
                .orElseThrow(() -> new TimeEntryNotFoundException(id));
        timetrackAccess.requireCanView(entry.getEmployeeId());

        return timeEntryAuditRepository.findAllByTimeEntryIdOrderByCreatedAtDesc(id)
                .stream()
                .map(timeEntryAuditMapper::toResponseDto)
                .toList();
    }

    private void requireOwner(TimeEntry entry) {
        if (!entry.getEmployeeId().equals(timetrackAccess.currentEmployeeId())) {
            throw new AccessDeniedException("Изменять можно только собственную запись времени");
        }
    }

    private void requireDuration(Integer durationMinutes) {
        if (durationMinutes == null || durationMinutes <= 0 || durationMinutes > MAX_DAILY_MINUTES) {
            throw new ConflictException("Продолжительность должна быть от 1 до 1440 минут");
        }
    }

    private void requireOpenDate(UUID employeeId, LocalDate workDate) {
        if (!timesheetRepository.findCoveringDate(employeeId, workDate, LOCKED_TIMESHEET_STATUSES).isEmpty()) {
            throw new ConflictException("Дата входит в отправленный или утверждённый табель");
        }
    }

    private void requireDailyCapacity(UUID employeeId, LocalDate workDate, int durationMinutes, UUID excludedId) {
        employeeDayLock.lock(employeeId, workDate);

        long alreadyLogged = excludedId == null
                ? timeEntryRepository.sumDurationMinutes(employeeId, workDate, COUNTED_STATUSES)
                : timeEntryRepository.sumDurationMinutesExcluding(
                        employeeId,
                        workDate,
                        COUNTED_STATUSES,
                        excludedId
                );

        if (alreadyLogged + durationMinutes > MAX_DAILY_MINUTES) {
            throw new ConflictException("Суммарная продолжительность за день не может превышать 1440 минут");
        }
    }

    private void requireVersion(Long actual, Long expected) {
        if (expected != null && !expected.equals(actual)) {
            throw new ConflictException(ErrorMessages.CONCURRENT_MODIFICATION);
        }
    }

    private TimeEntry copy(TimeEntry entry) {
        return TimeEntry.builder()
                .projectId(entry.getProjectId())
                .taskId(entry.getTaskId())
                .projectAssignmentId(entry.getProjectAssignmentId())
                .workDate(entry.getWorkDate())
                .durationMinutes(entry.getDurationMinutes())
                .description(entry.getDescription())
                .status(entry.getStatus())
                .timesheetId(entry.getTimesheetId())
                .build();
    }
}
