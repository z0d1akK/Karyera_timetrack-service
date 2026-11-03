package com.zodiakk.timetrackservice.timesheet.service.impl;

import com.zodiakk.timetrackservice.common.constants.EventConstants;
import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.common.enums.TimeEntryAuditAction;
import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.enums.TimesheetReviewDecision;
import com.zodiakk.timetrackservice.common.enums.TimesheetStatus;
import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.common.time.Periods;
import com.zodiakk.timetrackservice.outbox.service.OutboxEventService;
import com.zodiakk.timetrackservice.schedule.service.ScheduleNormService;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.lock.EmployeeDayLock;
import com.zodiakk.timetrackservice.timeentry.mapper.TimeEntryMapper;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.timeentry.service.TimeEntryAuditWriter;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetApproveRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetRejectRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.action.TimesheetSubmitRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.create.TimesheetCreateRequestDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetFilterDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetReviewResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetSubmitResponseDto;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;
import com.zodiakk.timetrackservice.timesheet.entity.TimesheetReview;
import com.zodiakk.timetrackservice.timesheet.exception.TimesheetNotFoundException;
import com.zodiakk.timetrackservice.timesheet.mapper.TimesheetMapper;
import com.zodiakk.timetrackservice.timesheet.mapper.TimesheetReviewMapper;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetReviewRepository;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet.TimesheetPageableFactory;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet.TimesheetSpecification;
import com.zodiakk.timetrackservice.timesheet.service.TimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimesheetServiceImpl implements TimesheetService {

    private static final Set<TimeEntryStatus> SUBMITTABLE_STATUSES = EnumSet.of(
            TimeEntryStatus.DRAFT,
            TimeEntryStatus.REJECTED
    );

    private final TimesheetRepository timesheetRepository;

    private final TimesheetReviewRepository timesheetReviewRepository;

    private final TimeEntryRepository timeEntryRepository;

    private final TimesheetMapper timesheetMapper;

    private final TimesheetReviewMapper timesheetReviewMapper;

    private final TimeEntryMapper timeEntryMapper;

    private final TimeEntryAuditWriter timeEntryAuditWriter;

    private final ScheduleNormService scheduleNormService;

    private final OutboxEventService outboxEventService;

    private final EmployeeDayLock employeeDayLock;

    private final TimetrackAccess timetrackAccess;

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimesheetResponseDto create(TimesheetCreateRequestDto request) {
        Periods.requireWeek(request.getPeriodStart(), request.getPeriodEnd());
        UUID employeeId = timetrackAccess.currentEmployeeId();

        if (timesheetRepository.findByEmployeeIdAndPeriodStart(employeeId, request.getPeriodStart()).isPresent()) {
            throw new ConflictException("Табель за эту неделю уже существует");
        }

        ScheduleNormService.NormCalculation norm = scheduleNormService.calculate(
                employeeId,
                request.getPeriodStart(),
                request.getPeriodEnd()
        );

        Timesheet timesheet = Timesheet.builder()
                .employeeId(employeeId)
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .status(TimesheetStatus.DRAFT)
                .plannedMinutes(norm.plannedMinutes())
                .build();

        return toResponse(timesheetRepository.save(timesheet), false);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public TimesheetResponseDto get(UUID id) {
        Timesheet timesheet = requireTimesheet(id);
        timetrackAccess.requireCanView(timesheet.getEmployeeId());
        return toResponse(timesheet, true);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<TimesheetResponseDto> findAll(TimesheetFilterDto filter, PageRequestDto pageRequest) {
        TimesheetFilterDto safeFilter = filter == null ? new TimesheetFilterDto() : filter;
        Specification<Timesheet> specification = TimesheetSpecification.filter(safeFilter);
        Set<UUID> visible = timetrackAccess.visibleEmployeeIds();

        if (visible != null) {
            if (safeFilter.getEmployeeId() != null && !visible.contains(safeFilter.getEmployeeId())) {
                throw new AccessDeniedException("Нет доступа к табелям выбранного сотрудника");
            }

            specification = specification.and((root, query, criteriaBuilder) -> root.get("employeeId").in(visible));
        }

        return timesheetRepository.findAll(specification, TimesheetPageableFactory.create(pageRequest))
                .map(timesheet -> toResponse(timesheet, false));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TimesheetResponseDto> findByEmployee(UUID employeeId) {
        timetrackAccess.requireCanView(employeeId);

        return timesheetRepository.findAllByEmployeeIdOrderByPeriodStartDesc(employeeId)
                .stream()
                .map(timesheet -> toResponse(timesheet, false))
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimesheetSubmitResponseDto submit(UUID id, TimesheetSubmitRequestDto request) {
        Timesheet timesheet = timesheetRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new TimesheetNotFoundException(id));
        requireOwner(timesheet);
        requireVersion(timesheet.getVersion(), request.getExpectedVersion());

        if (timesheet.getStatus() != TimesheetStatus.DRAFT && timesheet.getStatus() != TimesheetStatus.REJECTED) {
            throw new ConflictException("Отправить можно только черновик или отклонённый табель");
        }

        lockPeriod(timesheet);
        List<TimeEntry> attached = attachEntries(timesheet);
        ScheduleNormService.NormCalculation norm = scheduleNormService.calculate(
                timesheet.getEmployeeId(),
                timesheet.getPeriodStart(),
                timesheet.getPeriodEnd()
        );

        long actualMinutes = attached.stream().mapToLong(TimeEntry::getDurationMinutes).sum();
        List<String> warnings = new ArrayList<>(norm.warnings());

        if (attached.isEmpty()) {
            warnings.add("За неделю нет записей рабочего времени");
        }

        if (actualMinutes != norm.plannedMinutes()) {
            warnings.add("Зафиксировано %d минут при норме %d".formatted(actualMinutes, norm.plannedMinutes()));
        }

        timesheet.setPlannedMinutes(norm.plannedMinutes());
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        timesheet.setSubmittedAt(OffsetDateTime.now(ZoneOffset.UTC));
        timesheet.setApprovedAt(null);
        timesheet.setApprovedBy(null);

        Timesheet saved = timesheetRepository.save(timesheet);
        publish(
                saved,
                EventConstants.TIMESHEET_SUBMITTED,
                actualMinutes,
                attached.size(),
                List.of()
        );

        return TimesheetSubmitResponseDto.builder()
                .timesheet(toResponse(saved, true))
                .warnings(List.copyOf(warnings))
                .build();
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimesheetResponseDto approve(UUID id, TimesheetApproveRequestDto request) {
        Timesheet timesheet = timesheetRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new TimesheetNotFoundException(id));
        timetrackAccess.requireCanApprove(timesheet.getEmployeeId());
        requireVersion(timesheet.getVersion(), request.getExpectedVersion());
        requireSubmitted(timesheet);

        List<TimeEntry> entries = transitionEntries(timesheet, TimeEntryStatus.APPROVED, TimeEntryAuditAction.APPROVED);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        timesheet.setStatus(TimesheetStatus.APPROVED);
        timesheet.setApprovedAt(now);
        timesheet.setApprovedBy(timetrackAccess.currentEmployeeId());

        saveReview(timesheet, TimesheetReviewDecision.APPROVE, request.getComment(), now);
        Timesheet saved = timesheetRepository.save(timesheet);
        long approvedMinutes = entries.stream().mapToLong(TimeEntry::getDurationMinutes).sum();
        publish(saved, EventConstants.TIMESHEET_APPROVED, approvedMinutes, entries.size(), entryPayloads(entries));
        return toResponse(saved, true);
    }

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public TimesheetResponseDto reject(UUID id, TimesheetRejectRequestDto request) {
        Timesheet timesheet = timesheetRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new TimesheetNotFoundException(id));
        timetrackAccess.requireCanApprove(timesheet.getEmployeeId());
        requireVersion(timesheet.getVersion(), request.getExpectedVersion());
        requireSubmitted(timesheet);

        List<TimeEntry> entries = transitionEntries(timesheet, TimeEntryStatus.REJECTED, TimeEntryAuditAction.REJECTED);
        timesheet.setStatus(TimesheetStatus.REJECTED);
        timesheet.setApprovedAt(null);
        timesheet.setApprovedBy(null);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        saveReview(timesheet, TimesheetReviewDecision.REJECT, request.getComment(), now);
        Timesheet saved = timesheetRepository.save(timesheet);
        long minutes = entries.stream().mapToLong(TimeEntry::getDurationMinutes).sum();
        publish(saved, EventConstants.TIMESHEET_REJECTED, minutes, entries.size(), List.of());
        return toResponse(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TimesheetReviewResponseDto> reviews(UUID id) {
        Timesheet timesheet = requireTimesheet(id);
        timetrackAccess.requireCanView(timesheet.getEmployeeId());

        return timesheetReviewRepository.findAllByTimesheetIdOrderByReviewedAtDesc(id)
                .stream()
                .map(timesheetReviewMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<TimesheetResponseDto> pendingApproval() {
        timetrackAccess.requireCanReviewQueue();
        UUID actorEmployeeId = timetrackAccess.currentEmployeeId();
        List<Timesheet> timesheets;

        if (timetrackAccess.isHrOrAdmin()) {
            timesheets = timesheetRepository.findAllByStatusOrderBySubmittedAtAsc(TimesheetStatus.SUBMITTED)
                    .stream()
                    .filter(timesheet -> !timesheet.getEmployeeId().equals(actorEmployeeId))
                    .toList();
        } else {
            List<UUID> subordinateIds = timetrackAccess.visibleEmployeeIds().stream()
                    .filter(employeeId -> !employeeId.equals(actorEmployeeId))
                    .toList();

            timesheets = subordinateIds.isEmpty()
                    ? List.of()
                    : timesheetRepository.findAllByStatusAndEmployeeIdIn(TimesheetStatus.SUBMITTED, subordinateIds);
        }

        return timesheets.stream()
                .map(timesheet -> toResponse(timesheet, false))
                .toList();
    }

    private List<TimeEntry> attachEntries(Timesheet timesheet) {
        UUID actorEmployeeId = timetrackAccess.currentEmployeeId();
        List<TimeEntry> weekEntries = timeEntryRepository.findAllByEmployeeIdAndWorkDateBetween(
                timesheet.getEmployeeId(),
                timesheet.getPeriodStart(),
                timesheet.getPeriodEnd()
        );
        List<TimeEntry> attached = new ArrayList<>();

        for (TimeEntry entry : weekEntries) {
            if (entry.getStatus() == TimeEntryStatus.VOIDED) {
                continue;
            }

            if (entry.getTimesheetId() != null && !entry.getTimesheetId().equals(timesheet.getId())) {
                throw new ConflictException("Запись времени уже включена в другой табель");
            }

            if (!SUBMITTABLE_STATUSES.contains(entry.getStatus())) {
                throw new ConflictException("В неделю входит запись, которую нельзя отправить на согласование");
            }

            TimeEntryStatus previousStatus = entry.getStatus();
            UUID previousTimesheetId = entry.getTimesheetId();
            entry.setTimesheetId(timesheet.getId());
            entry.setStatus(TimeEntryStatus.SUBMITTED);
            Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("status", Map.of("old", previousStatus, "new", TimeEntryStatus.SUBMITTED));
            Map<String, Object> timesheetChange = new LinkedHashMap<>();
            timesheetChange.put("old", previousTimesheetId);
            timesheetChange.put("new", timesheet.getId());
            changes.put("timesheetId", timesheetChange);
            timeEntryAuditWriter.action(
                    entry,
                    actorEmployeeId,
                    TimeEntryAuditAction.SUBMITTED,
                    changes
            );
            attached.add(entry);
        }

        timeEntryRepository.saveAll(attached);
        return attached;
    }

    private List<TimeEntry> transitionEntries(
            Timesheet timesheet,
            TimeEntryStatus targetStatus,
            TimeEntryAuditAction action
    ) {
        UUID actorEmployeeId = timetrackAccess.currentEmployeeId();
        List<TimeEntry> entries = timeEntryRepository.findAllByTimesheetId(timesheet.getId());
        List<TimeEntry> changed = new ArrayList<>();

        for (TimeEntry entry : entries) {
            if (entry.getStatus() == TimeEntryStatus.VOIDED) {
                continue;
            }

            if (entry.getStatus() != TimeEntryStatus.SUBMITTED) {
                throw new ConflictException("В табеле есть запись не в статусе отправки");
            }

            entry.setStatus(targetStatus);
            timeEntryAuditWriter.action(
                    entry,
                    actorEmployeeId,
                    action,
                    Map.of("status", Map.of("old", TimeEntryStatus.SUBMITTED, "new", targetStatus))
            );
            changed.add(entry);
        }

        timeEntryRepository.saveAll(changed);
        return changed;
    }

    private void saveReview(
            Timesheet timesheet,
            TimesheetReviewDecision decision,
            String comment,
            OffsetDateTime reviewedAt
    ) {
        timesheetReviewRepository.save(TimesheetReview.builder()
                .timesheetId(timesheet.getId())
                .reviewerEmployeeId(timetrackAccess.currentEmployeeId())
                .decision(decision)
                .comment(comment)
                .reviewedAt(reviewedAt)
                .build());
    }

    private void publish(
            Timesheet timesheet,
            String eventType,
            long minutes,
            int entryCount,
            List<Map<String, Object>> entries
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("timesheetId", timesheet.getId());
        payload.put("employeeId", timesheet.getEmployeeId());
        payload.put("periodStart", timesheet.getPeriodStart());
        payload.put("periodEnd", timesheet.getPeriodEnd());
        payload.put("plannedMinutes", timesheet.getPlannedMinutes());
        payload.put(eventType.equals(EventConstants.TIMESHEET_APPROVED) ? "approvedMinutes" : "minutes", minutes);
        payload.put("entryCount", entryCount);

        if (!entries.isEmpty()) {
            payload.put("entries", entries);
        }

        outboxEventService.saveEvent(
                timesheet.getId(),
                EventConstants.AGGREGATE_TIMESHEET,
                eventType,
                EventConstants.DEFAULT_EVENT_VERSION,
                payload
        );
    }

    private List<Map<String, Object>> entryPayloads(List<TimeEntry> entries) {
        return entries.stream()
                .map(entry -> {
                    Map<String, Object> payload = new LinkedHashMap<>();
                    payload.put("timeEntryId", entry.getId());
                    payload.put("projectId", entry.getProjectId());
                    payload.put("taskId", entry.getTaskId());
                    payload.put("projectAssignmentId", entry.getProjectAssignmentId());
                    payload.put("workDate", entry.getWorkDate());
                    payload.put("durationMinutes", entry.getDurationMinutes());
                    return payload;
                })
                .toList();
    }

    private void lockPeriod(Timesheet timesheet) {
        for (LocalDate date = timesheet.getPeriodStart(); !date.isAfter(timesheet.getPeriodEnd()); date = date.plusDays(1)) {
            employeeDayLock.lock(timesheet.getEmployeeId(), date);
        }
    }

    private void requireOwner(Timesheet timesheet) {
        if (!timesheet.getEmployeeId().equals(timetrackAccess.currentEmployeeId())) {
            throw new AccessDeniedException("Отправить можно только собственный табель");
        }
    }

    private void requireSubmitted(Timesheet timesheet) {
        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new ConflictException("Решение можно принять только по отправленному табелю");
        }
    }

    private void requireVersion(Long actual, Long expected) {
        if (expected == null || !expected.equals(actual)) {
            throw new ConflictException(ErrorMessages.CONCURRENT_MODIFICATION);
        }
    }

    private Timesheet requireTimesheet(UUID id) {
        return timesheetRepository.findById(id)
                .orElseThrow(() -> new TimesheetNotFoundException(id));
    }

    private TimesheetResponseDto toResponse(Timesheet timesheet, boolean includeEntries) {
        TimesheetResponseDto response = timesheetMapper.toResponseDto(timesheet);
        List<TimeEntry> entries = timeEntryRepository.findAllByTimesheetId(timesheet.getId());
        long totalMinutes = entries.stream()
                .filter(entry -> entry.getStatus() != TimeEntryStatus.VOIDED)
                .mapToLong(TimeEntry::getDurationMinutes)
                .sum();
        response.setTotalMinutes(totalMinutes);

        if (includeEntries) {
            response.setEntries(entries.stream().map(timeEntryMapper::toResponseDto).toList());
        }

        return response;
    }
}
