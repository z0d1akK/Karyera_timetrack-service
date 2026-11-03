package com.zodiakk.timetrackservice.timeentry.lock;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeDayLock {

    private final EntityManager entityManager;

    public void lock(UUID employeeId, LocalDate workDate) {
        int employeeKey = (int) employeeId.getLeastSignificantBits();
        int dateKey = (int) workDate.toEpochDay();

        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(:employeeKey, :dateKey)")
                .setParameter("employeeKey", employeeKey)
                .setParameter("dateKey", dateKey)
                .getResultList();
    }
}
