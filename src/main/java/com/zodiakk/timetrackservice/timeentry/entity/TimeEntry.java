package com.zodiakk.timetrackservice.timeentry.entity;

import com.zodiakk.timetrackservice.common.entity.VersionedCreatedByEntity;
import com.zodiakk.timetrackservice.common.enums.TimeEntryStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "time_entries")
@NoArgsConstructor
@SuperBuilder
public class TimeEntry extends VersionedCreatedByEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "project_assignment_id")
    private UUID projectAssignmentId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TimeEntryStatus status;

    @Column(name = "timesheet_id")
    private UUID timesheetId;

}
