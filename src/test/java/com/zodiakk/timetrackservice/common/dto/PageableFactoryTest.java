package com.zodiakk.timetrackservice.common.dto;

import com.zodiakk.timetrackservice.schedule.repository.specification.employeeWorkSchedule.EmployeeWorkSchedulePageableFactory;
import com.zodiakk.timetrackservice.schedule.repository.specification.workSchedule.WorkSchedulePageableFactory;
import com.zodiakk.timetrackservice.schedule.repository.specification.workScheduleDay.WorkScheduleDayPageableFactory;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry.TimeEntryPageableFactory;
import com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntryAudit.TimeEntryAuditPageableFactory;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet.TimesheetPageableFactory;
import com.zodiakk.timetrackservice.timesheet.repository.specification.timesheetReview.TimesheetReviewPageableFactory;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageableFactoryTest {

    @Test
    void factoriesWhitelistSortFields() {
        PageRequestDto request = new PageRequestDto();
        request.setPage(1);
        request.setSize(10);
        request.setSortBy("unknown");
        request.setSortDirection(SortDirection.DESC);

        Pageable schedules = WorkSchedulePageableFactory.create(request);
        Pageable entries = TimeEntryPageableFactory.create(request);
        Pageable timesheets = TimesheetPageableFactory.create(request);
        request.setSortBy("validFrom");
        Pageable assignments = EmployeeWorkSchedulePageableFactory.create(request);

        assertEquals("name", schedules.getSort().iterator().next().getProperty());
        assertEquals(Sort.Direction.DESC, schedules.getSort().iterator().next().getDirection());
        assertEquals("workDate", entries.getSort().iterator().next().getProperty());
        assertEquals("periodStart", timesheets.getSort().iterator().next().getProperty());
        assertEquals("validFrom", assignments.getSort().iterator().next().getProperty());

        PageRequestDto ascending = new PageRequestDto();
        ascending.setSortBy("createdAt");
        ascending.setSortDirection(SortDirection.ASC);
        assertEquals("createdAt", TimeEntryAuditPageableFactory.create(ascending).getSort().iterator().next().getProperty());
        assertEquals(Sort.Direction.ASC, TimeEntryAuditPageableFactory.create(ascending).getSort().getOrderFor("createdAt").getDirection());
        ascending.setSortBy("reviewedAt");
        assertEquals("reviewedAt", TimesheetReviewPageableFactory.create(ascending).getSort().iterator().next().getProperty());
        ascending.setSortBy("dayOfWeek");
        assertEquals("dayOfWeek", WorkScheduleDayPageableFactory.create(ascending).getSort().iterator().next().getProperty());
        ascending.setSortBy("missing");
        assertEquals("createdAt", TimeEntryAuditPageableFactory.create(ascending).getSort().iterator().next().getProperty());
        assertEquals("reviewedAt", TimesheetReviewPageableFactory.create(ascending).getSort().iterator().next().getProperty());
        assertEquals("dayOfWeek", WorkScheduleDayPageableFactory.create(ascending).getSort().iterator().next().getProperty());
    }
}
