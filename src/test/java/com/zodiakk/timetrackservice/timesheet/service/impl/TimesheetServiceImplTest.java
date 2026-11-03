package com.zodiakk.timetrackservice.timesheet.service.impl;

import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.enums.TimesheetStatus;
import com.zodiakk.timetrackservice.common.exception.ConflictException;
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
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetResponseDto;
import com.zodiakk.timetrackservice.timesheet.dto.response.TimesheetSubmitResponseDto;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;
import com.zodiakk.timetrackservice.timesheet.mapper.TimesheetMapper;
import com.zodiakk.timetrackservice.timesheet.mapper.TimesheetReviewMapper;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimesheetServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");
    private static final LocalDate WEEK_START = LocalDate.of(2026, 10, 19);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 10, 25);

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private TimesheetReviewRepository timesheetReviewRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private TimesheetMapper timesheetMapper;

    @Mock
    private TimesheetReviewMapper timesheetReviewMapper;

    @Mock
    private TimeEntryMapper timeEntryMapper;

    @Mock
    private TimeEntryAuditWriter timeEntryAuditWriter;

    @Mock
    private ScheduleNormService scheduleNormService;

    @Mock
    private OutboxEventService outboxEventService;

    @Mock
    private EmployeeDayLock employeeDayLock;

    @Mock
    private TimetrackAccess timetrackAccess;

    @InjectMocks
    private TimesheetServiceImpl timesheetService;

    @Test
    void submitRejectsApprovedTimesheet() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.APPROVED);
        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);

        assertThrows(ConflictException.class, () -> timesheetService.submit(timesheet.getId(), submitRequest(0L)));
        verify(timeEntryRepository, never()).saveAll(any());
    }

    @Test
    void submitAttachesDraftEntriesAndPublishesEvent() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.DRAFT);
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setEmployeeId(EMPLOYEE_ID);
        entry.setStatus(TimeEntryStatus.DRAFT);
        entry.setDurationMinutes(120);
        entry.setWorkDate(WEEK_START);

        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);
        when(timeEntryRepository.findAllByEmployeeIdAndWorkDateBetween(EMPLOYEE_ID, WEEK_START, WEEK_END))
                .thenReturn(List.of(entry));
        when(scheduleNormService.calculate(EMPLOYEE_ID, WEEK_START, WEEK_END))
                .thenReturn(new ScheduleNormService.NormCalculation(2400, List.of(), List.of()));
        when(timesheetRepository.save(timesheet)).thenReturn(timesheet);
        when(timeEntryRepository.findAllByTimesheetId(timesheet.getId())).thenReturn(List.of(entry));
        when(timesheetMapper.toResponseDto(timesheet)).thenReturn(new TimesheetResponseDto());
        when(timeEntryMapper.toResponseDto(entry)).thenReturn(new com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto());

        TimesheetSubmitResponseDto response = timesheetService.submit(timesheet.getId(), submitRequest(0L));

        assertEquals(TimesheetStatus.SUBMITTED, timesheet.getStatus());
        assertEquals(TimeEntryStatus.SUBMITTED, entry.getStatus());
        assertEquals(timesheet.getId(), entry.getTimesheetId());
        assertEquals(2400, timesheet.getPlannedMinutes());
        assertTrue(response.getWarnings().stream().anyMatch(warning -> warning.contains("2400")));
        verify(outboxEventService).saveEvent(any(), any(), any(), any(Integer.class), any());
    }

    @Test
    void approveRejectsSelfApproval() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.SUBMITTED);
        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        doThrow(new AccessDeniedException("self")).when(timetrackAccess).requireCanApprove(EMPLOYEE_ID);

        assertThrows(
                AccessDeniedException.class,
                () -> timesheetService.approve(timesheet.getId(), approveRequest())
        );
        verify(timesheetRepository, never()).save(any());
    }

    @Test
    void submitRejectsForeignOwner() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.DRAFT);
        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        when(timetrackAccess.currentEmployeeId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class, () -> timesheetService.submit(timesheet.getId(), submitRequest(0L)));
    }

    @Test
    void submitRejectsStaleVersion() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.DRAFT);
        timesheet.setVersion(3L);
        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);

        assertThrows(ConflictException.class, () -> timesheetService.submit(timesheet.getId(), submitRequest(0L)));
    }

    @Test
    void rejectReturnsEntriesToRejected() {
        Timesheet timesheet = weekTimesheet(TimesheetStatus.SUBMITTED);
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setStatus(TimeEntryStatus.SUBMITTED);
        entry.setDurationMinutes(60);
        entry.setTimesheetId(timesheet.getId());

        when(timesheetRepository.findByIdForUpdate(timesheet.getId())).thenReturn(Optional.of(timesheet));
        when(timetrackAccess.currentEmployeeId()).thenReturn(UUID.randomUUID());
        when(timeEntryRepository.findAllByTimesheetId(timesheet.getId())).thenReturn(List.of(entry));
        when(timesheetRepository.save(timesheet)).thenReturn(timesheet);
        when(timesheetMapper.toResponseDto(timesheet)).thenReturn(new TimesheetResponseDto());

        TimesheetRejectRequestDto request = new TimesheetRejectRequestDto();
        request.setExpectedVersion(0L);
        request.setComment("Нужны пояснения");

        timesheetService.reject(timesheet.getId(), request);

        assertEquals(TimesheetStatus.REJECTED, timesheet.getStatus());
        assertEquals(TimeEntryStatus.REJECTED, entry.getStatus());
        verify(timesheetReviewRepository).save(any());
        verify(outboxEventService).saveEvent(any(), any(), any(), any(Integer.class), any());
    }

    private Timesheet weekTimesheet(TimesheetStatus status) {
        Timesheet timesheet = new Timesheet();
        timesheet.setId(UUID.randomUUID());
        timesheet.setEmployeeId(EMPLOYEE_ID);
        timesheet.setPeriodStart(WEEK_START);
        timesheet.setPeriodEnd(WEEK_END);
        timesheet.setStatus(status);
        timesheet.setVersion(0L);
        timesheet.setPlannedMinutes(0);
        return timesheet;
    }

    private TimesheetSubmitRequestDto submitRequest(long version) {
        TimesheetSubmitRequestDto request = new TimesheetSubmitRequestDto();
        request.setExpectedVersion(version);
        return request;
    }

    private TimesheetApproveRequestDto approveRequest() {
        TimesheetApproveRequestDto request = new TimesheetApproveRequestDto();
        request.setExpectedVersion(0L);
        request.setComment("Подтверждено");
        return request;
    }
}
