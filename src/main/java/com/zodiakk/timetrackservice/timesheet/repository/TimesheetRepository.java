package com.zodiakk.timetrackservice.timesheet.repository;

import com.zodiakk.timetrackservice.common.enums.*;
import com.zodiakk.timetrackservice.timesheet.entity.Timesheet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, UUID>, JpaSpecificationExecutor<Timesheet> {

    List<Timesheet> findAllByEmployeeIdOrderByPeriodStartDesc(UUID employeeId);

    Optional<Timesheet> findByEmployeeIdAndPeriodStart(UUID employeeId, LocalDate periodStart);

    List<Timesheet> findAllByStatus(TimesheetStatus status);

    List<Timesheet> findAllByStatusOrderBySubmittedAtAsc(TimesheetStatus status);

    List<Timesheet> findAllByStatusAndEmployeeIdIn(TimesheetStatus status, Collection<UUID> employeeIds);

    @Query("""
            SELECT t FROM Timesheet t
            WHERE t.employeeId = :employeeId
              AND t.periodStart <= :date
              AND t.periodEnd >= :date
              AND t.status IN :statuses
            """)
    List<Timesheet> findCoveringDate(
            @Param("employeeId") UUID employeeId,
            @Param("date") LocalDate date,
            @Param("statuses") Collection<TimesheetStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Timesheet t WHERE t.id = :timesheetId")
    Optional<Timesheet> findByIdForUpdate(@Param("timesheetId") UUID timesheetId);

}
