package com.zodiakk.timetrackservice.repository;

import com.zodiakk.timetrackservice.common.AbstractIntegrationTest;
import com.zodiakk.timetrackservice.common.enums.TimeEntryAuditAction;
import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import com.zodiakk.timetrackservice.common.enums.TimesheetReviewDecision;
import com.zodiakk.timetrackservice.common.enums.TimesheetStatus;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.EmployeeWorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleDayFilterDto;
import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.repository.EmployeeWorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleDayRepository;
import com.zodiakk.timetrackservice.schedule.repository.WorkScheduleRepository;
import com.zodiakk.timetrackservice.schedule.repository.specification.employeeWorkSchedule.EmployeeWorkScheduleSpecification;
import com.zodiakk.timetrackservice.schedule.repository.specification.workSchedule.WorkScheduleSpecification;
import com.zodiakk.timetrackservice.schedule.repository.specification.workScheduleDay.WorkScheduleDaySpecification;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryAuditFilterDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryAuditRepository;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryRepository;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntrySpecification;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntryAudit.TimeEntryAuditSpecification;
import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetFilterDto;
import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetReviewFilterDto;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetRepository;
import com.zodiakk.timetrackservice.timesheet.repository.TimesheetReviewRepository;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet.TimesheetSpecification;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheetReview.TimesheetReviewSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private WorkScheduleDayRepository workScheduleDayRepository;

    @Autowired
    private EmployeeWorkScheduleRepository employeeWorkScheduleRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @Autowired
    private TimeEntryAuditRepository timeEntryAuditRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private TimesheetReviewRepository timesheetReviewRepository;

    @Test
    void specificationsAcceptEmptyAndPopulatedFilters() {
        assertThat(workScheduleRepository.findAll(WorkScheduleSpecification.filter(null))).isNotNull();
        assertThat(workScheduleRepository.findAll(WorkScheduleSpecification.filter(scheduleFilter()))).isEmpty();

        assertThat(workScheduleDayRepository.findAll(WorkScheduleDaySpecification.filter(null))).isNotNull();
        WorkScheduleDayFilterDto dayFilter = new WorkScheduleDayFilterDto();
        dayFilter.setScheduleId(UUID.randomUUID());
        dayFilter.setDayOfWeek((short) 1);
        assertThat(workScheduleDayRepository.findAll(WorkScheduleDaySpecification.filter(dayFilter))).isEmpty();

        assertThat(employeeWorkScheduleRepository.findAll(EmployeeWorkScheduleSpecification.filter(null))).isNotNull();
        EmployeeWorkScheduleFilterDto assignmentFilter = new EmployeeWorkScheduleFilterDto();
        assignmentFilter.setEmployeeId(UUID.randomUUID());
        assignmentFilter.setScheduleId(UUID.randomUUID());
        assignmentFilter.setValidFromFrom(LocalDate.of(2026, 1, 1));
        assignmentFilter.setValidFromTo(LocalDate.of(2026, 12, 31));
        assertThat(employeeWorkScheduleRepository.findAll(EmployeeWorkScheduleSpecification.filter(assignmentFilter))).isEmpty();

        assertThat(timeEntryRepository.findAll(TimeEntrySpecification.filter(null))).isNotNull();
        assertThat(timeEntryRepository.findAll(TimeEntrySpecification.filter(entryFilter()))).isEmpty();

        assertThat(timeEntryAuditRepository.findAll(TimeEntryAuditSpecification.filter(null))).isNotNull();
        TimeEntryAuditFilterDto auditFilter = new TimeEntryAuditFilterDto();
        auditFilter.setTimeEntryId(UUID.randomUUID());
        auditFilter.setActorEmployeeId(UUID.randomUUID());
        auditFilter.setAction(TimeEntryAuditAction.CREATED);
        assertThat(timeEntryAuditRepository.findAll(TimeEntryAuditSpecification.filter(auditFilter))).isEmpty();

        assertThat(timesheetRepository.findAll(TimesheetSpecification.filter(null))).isNotNull();
        assertThat(timesheetRepository.findAll(TimesheetSpecification.filter(timesheetFilter()))).isEmpty();

        assertThat(timesheetReviewRepository.findAll(TimesheetReviewSpecification.filter(null))).isNotNull();
        TimesheetReviewFilterDto reviewFilter = new TimesheetReviewFilterDto();
        reviewFilter.setTimesheetId(UUID.randomUUID());
        reviewFilter.setReviewerEmployeeId(UUID.randomUUID());
        reviewFilter.setDecision(TimesheetReviewDecision.REJECT);
        assertThat(timesheetReviewRepository.findAll(TimesheetReviewSpecification.filter(reviewFilter))).isEmpty();
    }

    private WorkScheduleFilterDto scheduleFilter() {
        WorkScheduleFilterDto filter = new WorkScheduleFilterDto();
        filter.setCode(" ");
        filter.setName("week");
        filter.setTimezone("Europe");
        filter.setActive(true);
        return filter;
    }

    private TimeEntryFilterDto entryFilter() {
        TimeEntryFilterDto filter = new TimeEntryFilterDto();
        filter.setEmployeeId(UUID.randomUUID());
        filter.setProjectId(UUID.randomUUID());
        filter.setTaskId(UUID.randomUUID());
        filter.setTimesheetId(UUID.randomUUID());
        filter.setStatus(TimeEntryStatus.DRAFT);
        filter.setWorkDateFrom(LocalDate.of(2026, 10, 1));
        filter.setWorkDateTo(LocalDate.of(2026, 10, 31));
        return filter;
    }

    private TimesheetFilterDto timesheetFilter() {
        TimesheetFilterDto filter = new TimesheetFilterDto();
        filter.setEmployeeId(UUID.randomUUID());
        filter.setStatus(TimesheetStatus.DRAFT);
        filter.setPeriodStartFrom(LocalDate.of(2026, 1, 1));
        filter.setPeriodStartTo(LocalDate.of(2026, 6, 1));
        filter.setPeriodEndFrom(LocalDate.of(2026, 1, 1));
        filter.setPeriodEndTo(LocalDate.of(2026, 12, 31));
        return filter;
    }
}
