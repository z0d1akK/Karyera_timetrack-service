package com.zodiakk.timetrackservice.timesheet.repository.specification.timesheetReview;

import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetReviewFilterDto;
import com.zodiakk.timetrackservice.timesheet.entity.TimesheetReview;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class TimesheetReviewSpecification {

    public static Specification<TimesheetReview> filter(TimesheetReviewFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getTimesheetId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("timesheetId"), filter.getTimesheetId()));
            }

            if (filter.getReviewerEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("reviewerEmployeeId"), filter.getReviewerEmployeeId()));
            }

            if (filter.getDecision() != null) {
                predicates.add(criteriaBuilder.equal(root.get("decision"), filter.getDecision()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
