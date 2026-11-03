package com.zodiakk.timetrackservice.timesheet.repository.specification.timesheet;

import com.zodiakk.timetrackservice.timesheet.dto.request.filter.TimesheetFilterDto;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class TimesheetSpecification {

    public static Specification<Timesheet> filter(TimesheetFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employeeId"), filter.getEmployeeId()));
            }

            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            if (filter.getPeriodStartFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("periodStart"), filter.getPeriodStartFrom()));
            }

            if (filter.getPeriodStartTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("periodStart"), filter.getPeriodStartTo()));
            }

            if (filter.getPeriodEndFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("periodEnd"), filter.getPeriodEndFrom()));
            }

            if (filter.getPeriodEndTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("periodEnd"), filter.getPeriodEndTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
