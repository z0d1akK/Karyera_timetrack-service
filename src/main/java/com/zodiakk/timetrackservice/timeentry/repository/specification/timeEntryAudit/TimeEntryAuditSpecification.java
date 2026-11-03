package com.zodiakk.timetrackservice.timeentry.repository.specification.timeEntryAudit;

import com.zodiakk.timetrackservice.timeentry.dto.request.filter.TimeEntryAuditFilterDto;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntryAudit;

import jakarta.persistence.criteria.*;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

@UtilityClass
public class TimeEntryAuditSpecification {

    public static Specification<TimeEntryAudit> filter(TimeEntryAuditFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return criteriaBuilder.conjunction();
            }

            if (filter.getTimeEntryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("timeEntryId"), filter.getTimeEntryId()));
            }

            if (filter.getActorEmployeeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("actorEmployeeId"), filter.getActorEmployeeId()));
            }

            if (filter.getAction() != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), filter.getAction()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
