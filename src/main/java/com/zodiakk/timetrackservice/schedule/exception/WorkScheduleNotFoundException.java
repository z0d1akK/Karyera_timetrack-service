package com.zodiakk.timetrackservice.schedule.exception;

import com.zodiakk.timetrackservice.common.exception.ErrorMessages;
import com.zodiakk.timetrackservice.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class WorkScheduleNotFoundException extends ResourceNotFoundException {

    public WorkScheduleNotFoundException(UUID id) {
        super(ErrorMessages.WORK_SCHEDULE_NOT_FOUND.formatted(id));
    }
}
