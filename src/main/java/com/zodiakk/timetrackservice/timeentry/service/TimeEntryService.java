package com.zodiakk.timetrackservice.timeentry.service;

import com.zodiakk.timetrackservice.common.dto.PageRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.action.TimeEntryVoidRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.create.TimeEntryCreateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.dto.request.update.TimeEntryUpdateRequestDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryAuditResponseDto;
import com.zodiakk.timetrackservice.timeentry.dto.response.TimeEntryResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface TimeEntryService {

    TimeEntryResponseDto create(TimeEntryCreateRequestDto request);

    TimeEntryResponseDto update(UUID id, TimeEntryUpdateRequestDto request);

    TimeEntryResponseDto voidEntry(UUID id, TimeEntryVoidRequestDto request);

    TimeEntryResponseDto get(UUID id);

    Page<TimeEntryResponseDto> findAll(TimeEntryFilterDto filter, PageRequestDto pageRequest);

    List<TimeEntryAuditResponseDto> audit(UUID id);
}
