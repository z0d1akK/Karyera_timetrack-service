package com.zodiakk.timetrackservice.timeentry.service.impl;

import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.exception.ConflictException;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import com.zodiakk.timetrackservice.integration.project.service.ProjectDirectoryService;
import com.zodiakk.timetrackservice.security.access.TimetrackAccess;
import com.zodiakk.timetrackservice.timeentry.dto.request.action.TimeEntryVoidRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.lock.EmployeeDayLock;
import com.zodiakk.timetrackservice.timeentry.mapper.TimeEntryAuditMapper;
import com.zodiakk.timetrackservice.timeentry.mapper.TimeEntryMapper;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryAuditRepository;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.timeentry.service.TimeEntryAuditWriter;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryUpdateRequestDto;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceImplTest {

    private static final UUID EMPLOYEE_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");
    private static final UUID PROJECT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID TASK_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final LocalDate WORK_DATE = LocalDate.of(2026, 10, 19);

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private TimeEntryAuditRepository timeEntryAuditRepository;

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private TimeEntryMapper timeEntryMapper;

    @Mock
    private TimeEntryAuditMapper timeEntryAuditMapper;

    @Mock
    private TimeEntryAuditWriter timeEntryAuditWriter;

    @Mock
    private ProjectDirectoryService projectDirectory;

    @Mock
    private EmployeeDayLock employeeDayLock;

    @Mock
    private TimetrackAccess timetrackAccess;

    @InjectMocks
    private TimeEntryServiceImpl timeEntryService;

    @Test
    void createRejectsDurationThatExceedsTheDailyLimit() {
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);
        when(timesheetRepository.findCoveringDate(eq(EMPLOYEE_ID), eq(WORK_DATE), any())).thenReturn(List.of());
        when(projectDirectory.requireEligible(PROJECT_ID, TASK_ID, EMPLOYEE_ID, WORK_DATE)).thenReturn(eligibility());
        when(timeEntryRepository.sumDurationMinutes(eq(EMPLOYEE_ID), eq(WORK_DATE), any())).thenReturn(1400L);

        assertThrows(ConflictException.class, () -> timeEntryService.create(request(100)));

        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void createSavesDraftWhenDailyCapacityRemains() {
        TimeEntry mapped = new TimeEntry();
        TimeEntry saved = new TimeEntry();
        saved.setId(UUID.randomUUID());
        saved.setEmployeeId(EMPLOYEE_ID);
        saved.setStatus(TimeEntryStatus.DRAFT);
        TimeEntryResponseDto response = new TimeEntryResponseDto();

        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);
        when(timesheetRepository.findCoveringDate(eq(EMPLOYEE_ID), eq(WORK_DATE), any())).thenReturn(List.of());
        when(projectDirectory.requireEligible(PROJECT_ID, TASK_ID, EMPLOYEE_ID, WORK_DATE)).thenReturn(eligibility());
        when(timeEntryRepository.sumDurationMinutes(
                EMPLOYEE_ID,
                WORK_DATE,
                EnumSet.of(TimeEntryStatus.DRAFT, TimeEntryStatus.REJECTED, TimeEntryStatus.SUBMITTED, TimeEntryStatus.APPROVED)
        )).thenReturn(100L);
        when(timeEntryMapper.toEntity(any())).thenReturn(mapped);
        when(timeEntryRepository.save(mapped)).thenReturn(saved);
        when(timeEntryMapper.toResponseDto(saved)).thenReturn(response);

        TimeEntryResponseDto result = timeEntryService.create(request(50));

        assertEquals(response, result);
        assertEquals(TimeEntryStatus.DRAFT, mapped.getStatus());
        assertEquals(EMPLOYEE_ID, mapped.getEmployeeId());
        verify(timeEntryAuditWriter).created(saved, EMPLOYEE_ID);
    }

    @Test
    void voidRejectsApprovedEntry() {
        UUID entryId = UUID.randomUUID();
        TimeEntry entry = new TimeEntry();
        entry.setId(entryId);
        entry.setEmployeeId(EMPLOYEE_ID);
        entry.setStatus(TimeEntryStatus.APPROVED);
        entry.setVersion(2L);
        entry.setWorkDate(WORK_DATE);

        when(timeEntryRepository.findByIdForUpdate(entryId)).thenReturn(Optional.of(entry));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);

        TimeEntryVoidRequestDto request = new TimeEntryVoidRequestDto();
        request.setReason("Ошибочная запись");
        request.setExpectedVersion(2L);

        assertThrows(ConflictException.class, () -> timeEntryService.voidEntry(entryId, request));
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void updateRejectsForeignEntry() {
        TimeEntry entry = editableEntry();
        when(timeEntryRepository.findByIdForUpdate(entry.getId())).thenReturn(Optional.of(entry));
        when(timetrackAccess.currentEmployeeId()).thenReturn(UUID.randomUUID());

        assertThrows(AccessDeniedException.class, () -> timeEntryService.update(entry.getId(), new TimeEntryUpdateRequestDto()));
    }

    @Test
    void updateRejectsDateCoveredBySubmittedTimesheet() {
        TimeEntry entry = editableEntry();
        when(timeEntryRepository.findByIdForUpdate(entry.getId())).thenReturn(Optional.of(entry));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);
        when(timesheetRepository.findCoveringDate(eq(EMPLOYEE_ID), eq(WORK_DATE), any()))
                .thenReturn(List.of(new Timesheet()));

        assertThrows(ConflictException.class, () -> timeEntryService.update(entry.getId(), new TimeEntryUpdateRequestDto()));
        verify(timeEntryRepository, never()).save(any());
    }

    @Test
    void voidRejectsStaleVersion() {
        TimeEntry entry = editableEntry();
        entry.setVersion(4L);
        when(timeEntryRepository.findByIdForUpdate(entry.getId())).thenReturn(Optional.of(entry));
        when(timetrackAccess.currentEmployeeId()).thenReturn(EMPLOYEE_ID);

        TimeEntryVoidRequestDto request = new TimeEntryVoidRequestDto();
        request.setReason("Ошибка");
        request.setExpectedVersion(1L);

        assertThrows(ConflictException.class, () -> timeEntryService.voidEntry(entry.getId(), request));
    }

    private TimeEntry editableEntry() {
        TimeEntry entry = new TimeEntry();
        entry.setId(UUID.randomUUID());
        entry.setEmployeeId(EMPLOYEE_ID);
        entry.setProjectId(PROJECT_ID);
        entry.setTaskId(TASK_ID);
        entry.setWorkDate(WORK_DATE);
        entry.setDurationMinutes(60);
        entry.setStatus(TimeEntryStatus.DRAFT);
        entry.setVersion(1L);
        return entry;
    }

    private TimeEntryCreateRequestDto request(int minutes) {
        TimeEntryCreateRequestDto request = new TimeEntryCreateRequestDto();
        request.setProjectId(PROJECT_ID);
        request.setTaskId(TASK_ID);
        request.setWorkDate(WORK_DATE);
        request.setDurationMinutes(minutes);
        request.setDescription("Реализация API");
        return request;
    }

    private WorkEligibilityDto eligibility() {
        return new WorkEligibilityDto(
                true,
                PROJECT_ID,
                TASK_ID,
                EMPLOYEE_ID,
                WORK_DATE,
                UUID.randomUUID(),
                true,
                true,
                true,
                false,
                null
        );
    }
}
