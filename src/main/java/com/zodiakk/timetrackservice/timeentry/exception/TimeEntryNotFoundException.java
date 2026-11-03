package com.zodiakk.timetrackservice.timeentry.exception;

import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class TimeEntryNotFoundException extends ResourceNotFoundException {

    public TimeEntryNotFoundException(UUID id) {
        super(ErrorMessages.TIME_ENTRY_NOT_FOUND.formatted(id));
    }
}
