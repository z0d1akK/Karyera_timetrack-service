package com.zodiakk.timetrackservice.schedule.entity;

import com.zodiakk.timetrackservice.common.entity.BaseEntity;
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
@Table(name = "employee_work_schedules")
@NoArgsConstructor
@SuperBuilder
public class EmployeeWorkSchedule extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

}
