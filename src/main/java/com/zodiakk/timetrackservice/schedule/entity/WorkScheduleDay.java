package com.zodiakk.timetrackservice.schedule.entity;

import com.zodiakk.timetrackservice.common.entity.IdentifiedEntity;
import java.time.LocalTime;
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
@Table(name = "work_schedule_days")
@NoArgsConstructor
@SuperBuilder
public class WorkScheduleDay extends IdentifiedEntity {

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "planned_minutes", nullable = false)
    private Integer plannedMinutes;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

}
