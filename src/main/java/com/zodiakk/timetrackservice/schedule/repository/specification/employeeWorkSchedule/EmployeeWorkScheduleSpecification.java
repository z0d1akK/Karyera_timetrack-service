package com.zodiakk.timetrackservice.schedule.repository.specification.employeeWorkSchedule;

import com.zodiakk.timetrackservice.schedule.dto.request.filter.EmployeeWorkScheduleFilterDto;
import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class EmployeeWorkScheduleSpecification {

    public static Specification<EmployeeWorkSchedule> filter(EmployeeWorkScheduleFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("employeeId"), filter.getEmployeeId()));
            }

            if (filter.getScheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scheduleId"), filter.getScheduleId()));
            }

            if (filter.getValidFromFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("validFrom"), filter.getValidFromFrom()));
            }

            if (filter.getValidFromTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("validFrom"), filter.getValidFromTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
