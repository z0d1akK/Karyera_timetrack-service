package com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntry;

import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryFilterDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class TimeEntrySpecification {

    public static Specification<TimeEntry> filter(TimeEntryFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employeeId"), filter.getEmployeeId()));
            }

            if (filter.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("projectId"), filter.getProjectId()));
            }

            if (filter.getTaskId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("taskId"), filter.getTaskId()));
            }

            if (filter.getTimesheetId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("timesheetId"), filter.getTimesheetId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getWorkDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("workDate"), filter.getWorkDateFrom()));
            }

            if (filter.getWorkDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("workDate"), filter.getWorkDateTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
