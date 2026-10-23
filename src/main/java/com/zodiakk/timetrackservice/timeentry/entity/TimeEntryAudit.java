package com.zodiakk.timetrackservice.timeentry.entity;

import com.zodiakk.timetrackservice.common.entity.CreatedEntity;
import com.zodiakk.timetrackservice.common.enums.TimeEntryAuditAction;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import org.hibernate.annotations.ColumnTransformer;
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
@Table(name = "time_entry_audit")
@NoArgsConstructor
@SuperBuilder
public class TimeEntryAudit extends CreatedEntity {

    @Column(name = "time_entry_id", nullable = false)
    private UUID timeEntryId;

    @Column(name = "actor_employee_id", nullable = false)
    private UUID actorEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private TimeEntryAuditAction action;

    @ColumnTransformer(write = "?::jsonb")
    @Column(name = "changes", columnDefinition = "jsonb")
    private String changes;

}
