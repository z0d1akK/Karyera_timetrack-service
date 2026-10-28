package com.zodiakk.timetrackservice.timesheet.entity;

import com.zodiakk.timetrackservice.common.entity.IdentifiedEntity;
import com.zodiakk.timetrackservice.common.enums.TimesheetReviewDecision;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "timesheet_reviews")
@NoArgsConstructor
@SuperBuilder
public class TimesheetReview extends IdentifiedEntity {

    @Column(name = "timesheet_id", nullable = false)
    private UUID timesheetId;

    @Column(name = "reviewer_employee_id", nullable = false)
    private UUID reviewerEmployeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private TimesheetReviewDecision decision;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @Column(name = "reviewed_at", nullable = false)
    private OffsetDateTime reviewedAt;

}
