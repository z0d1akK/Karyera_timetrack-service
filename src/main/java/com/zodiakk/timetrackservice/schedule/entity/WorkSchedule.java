package com.zodiakk.timetrackservice.schedule.entity;

import com.zodiakk.timetrackservice.common.entity.VersionedEntity;
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
@Table(name = "work_schedules")
@NoArgsConstructor
@SuperBuilder
public class WorkSchedule extends VersionedEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "timezone", nullable = false, length = 100)
    private String timezone;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

}
