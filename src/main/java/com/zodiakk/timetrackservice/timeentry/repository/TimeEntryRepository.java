package com.zodiakk.timetrackservice.timeentry.repository;

import com.zodiakk.timetrackservice.common.enums.*;
import com.zodiakk.timetrackservice.timeentry.entity.TimeEntry;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.*;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID>, JpaSpecificationExecutor<TimeEntry> {

    List<TimeEntry> findAllByEmployeeIdAndWorkDateOrderByCreatedAtAsc(UUID employeeId, LocalDate workDate);

    List<TimeEntry> findAllByEmployeeIdAndWorkDateBetween(UUID employeeId, LocalDate from, LocalDate to);

    List<TimeEntry> findAllByProjectIdAndWorkDateBetween(UUID projectId, LocalDate from, LocalDate to);

    @Query("""
            SELECT DISTINCT e.employeeId
            FROM TimeEntry e
            WHERE e.workDate BETWEEN :from AND :to
            """)
    List<UUID> findDistinctEmployeeIdsByWorkDateBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    List<TimeEntry> findAllByTimesheetId(UUID timesheetId);

    List<TimeEntry> findAllByEmployeeIdAndTimesheetIdIsNull(UUID employeeId);

    long countByTimesheetId(UUID timesheetId);

    @Query("""
            SELECT COALESCE(SUM(e.durationMinutes), 0)
            FROM TimeEntry e
            WHERE e.employeeId = :employeeId
              AND e.workDate = :workDate
              AND e.status IN :statuses
            """)
    Long sumDurationMinutes(
            @Param("employeeId") UUID employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("statuses") Collection<TimeEntryStatus> statuses
    );

    @Query("""
            SELECT COALESCE(SUM(e.durationMinutes), 0)
            FROM TimeEntry e
            WHERE e.employeeId = :employeeId
              AND e.workDate = :workDate
              AND e.status IN :statuses
              AND e.id <> :excludedId
            """)
    Long sumDurationMinutesExcluding(
            @Param("employeeId") UUID employeeId,
            @Param("workDate") LocalDate workDate,
            @Param("statuses") Collection<TimeEntryStatus> statuses,
            @Param("excludedId") UUID excludedId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM TimeEntry e WHERE e.id = :entryId")
    Optional<TimeEntry> findByIdForUpdate(@Param("entryId") UUID entryId);

}
