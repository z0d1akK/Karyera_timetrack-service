package com.zodiakk.timetrackservice.timeentry.service;

import com.zodiakk.timetrackservice.common.enums.TimeEntryAuditAction;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntryAudit;
import com.zodiakk.timetrackservice.timeentry.repository.TimeEntryAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeEntryAuditWriter {

    private final TimeEntryAuditRepository timeEntryAuditRepository;

    private final ObjectMapper objectMapper;

    public void created(TimeEntry entry, UUID actorEmployeeId) {
        write(entry.getId(), actorEmployeeId, TimeEntryAuditAction.CREATED, snapshot(entry));
    }

    public void updated(TimeEntry before, TimeEntry after, UUID actorEmployeeId) {
        write(after.getId(), actorEmployeeId, TimeEntryAuditAction.UPDATED, diff(before, after));
    }

    public void action(TimeEntry entry, UUID actorEmployeeId, TimeEntryAuditAction action, Map<String, Object> changes) {
        write(entry.getId(), actorEmployeeId, action, changes);
    }

    public Map<String, Object> diff(TimeEntry before, TimeEntry after) {
        Map<String, Object> changes = new LinkedHashMap<>();
        putChange(changes, "projectId", before.getProjectId(), after.getProjectId());
        putChange(changes, "taskId", before.getTaskId(), after.getTaskId());
        putChange(changes, "projectAssignmentId", before.getProjectAssignmentId(), after.getProjectAssignmentId());
        putChange(changes, "workDate", before.getWorkDate(), after.getWorkDate());
        putChange(changes, "durationMinutes", before.getDurationMinutes(), after.getDurationMinutes());
        putChange(changes, "description", before.getDescription(), after.getDescription());
        putChange(changes, "status", before.getStatus(), after.getStatus());
        putChange(changes, "timesheetId", before.getTimesheetId(), after.getTimesheetId());
        return changes;
    }

    private Map<String, Object> snapshot(TimeEntry entry) {
        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put("projectId", change(null, entry.getProjectId()));
        changes.put("taskId", change(null, entry.getTaskId()));
        changes.put("workDate", change(null, entry.getWorkDate()));
        changes.put("durationMinutes", change(null, entry.getDurationMinutes()));
        changes.put("description", change(null, entry.getDescription()));
        changes.put("status", change(null, entry.getStatus()));
        return changes;
    }

    private Map<String, Object> change(Object oldValue, Object newValue) {
        Map<String, Object> change = new LinkedHashMap<>();
        change.put("old", oldValue);
        change.put("new", newValue);
        return change;
    }

    private void putChange(Map<String, Object> changes, String field, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.put(field, change(oldValue, newValue));
        }
    }

    private void write(UUID timeEntryId, UUID actorEmployeeId, TimeEntryAuditAction action, Map<String, Object> changes) {
        TimeEntryAudit audit = TimeEntryAudit.builder()
                .timeEntryId(timeEntryId)
                .actorEmployeeId(actorEmployeeId)
                .action(action)
                .changes(toJson(changes))
                .build();

        timeEntryAuditRepository.save(audit);
    }

    private String toJson(Map<String, Object> changes) {
        if (changes == null || changes.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(changes);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Не удалось записать аудит записи времени", ex);
        }
    }
}
