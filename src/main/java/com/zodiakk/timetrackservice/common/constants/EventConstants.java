package com.zodiakk.timetrackservice.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventConstants {

    public static final String AGGREGATE_TIMESHEET = "Timesheet";

    public static final String TIMESHEET_SUBMITTED = "TimesheetSubmitted";

    public static final String TIMESHEET_APPROVED = "TimesheetApproved";

    public static final String TIMESHEET_REJECTED = "TimesheetRejected";

    public static final int DEFAULT_EVENT_VERSION = 1;
}
