package com.zodiakk.timetrackservice.timesheet.exception;

import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class TimesheetNotFoundException extends ResourceNotFoundException {

    public TimesheetNotFoundException(UUID id) {
        super(ErrorMessages.TIMESHEET_NOT_FOUND.formatted(id));
    }
}
