package com.zodiakk.timetrackservice.schedule.repository;

import com.zodiakk.timetrackservice.schedule.entity.EmployeeWorkSchedule;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public interface EmployeeWorkScheduleRepository extends JpaRepository<EmployeeWorkSchedule, UUID>, JpaSpecificationExecutor<EmployeeWorkSchedule> {

    List<EmployeeWorkSchedule> findAllByEmployeeIdOrderByValidFromDesc(UUID employeeId);

    List<EmployeeWorkSchedule> findAllByScheduleId(UUID scheduleId);

    List<EmployeeWorkSchedule> findAllByEmployeeIdAndValidToIsNullOrValidToGreaterThanEqual(
            UUID employeeId,
            LocalDate date
    );

    @Query("""
            SELECT s FROM EmployeeWorkSchedule s
            WHERE s.employeeId = :employeeId
              AND s.validFrom <= :date
              AND (s.validTo IS NULL OR s.validTo >= :date)
            ORDER BY s.validFrom DESC
            """)
    List<EmployeeWorkSchedule> findActiveOnDate(
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM EmployeeWorkSchedule s WHERE s.id = :id")
    Optional<EmployeeWorkSchedule> findByIdForUpdate(@Param("id") UUID id);

}
