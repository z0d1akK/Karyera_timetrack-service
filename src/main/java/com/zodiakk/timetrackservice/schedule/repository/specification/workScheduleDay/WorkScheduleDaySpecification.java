package com.zodiakk.timetrackservice.schedule.repository.specification.workScheduleDay;

import com.zodiakk.timetrackservice.schedule.dto.request.filter.WorkScheduleDayFilterDto;
import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class WorkScheduleDaySpecification {

    public static Specification<WorkScheduleDay> filter(WorkScheduleDayFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getScheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("scheduleId"), filter.getScheduleId()));
            }

            if (filter.getDayOfWeek() != null) {
                predicates.add(criteriaBuilder.equal(root.get("dayOfWeek"), filter.getDayOfWeek()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
