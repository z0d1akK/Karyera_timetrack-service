package com.zodiakk.timetrackservice.schedule.repository;

import com.zodiakk.timetrackservice.schedule.entity.WorkScheduleDay;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface WorkScheduleDayRepository extends JpaRepository<WorkScheduleDay, UUID>, JpaSpecificationExecutor<WorkScheduleDay> {

    List<WorkScheduleDay> findAllByScheduleIdOrderByDayOfWeekAsc(UUID scheduleId);

    Optional<WorkScheduleDay> findByScheduleIdAndDayOfWeek(UUID scheduleId, Short dayOfWeek);

    boolean existsByScheduleIdAndDayOfWeek(UUID scheduleId, Short dayOfWeek);

    boolean existsByScheduleIdAndDayOfWeekAndIdNot(UUID scheduleId, Short dayOfWeek, UUID id);

    void deleteAllByScheduleId(UUID scheduleId);

    @Query("SELECT COALESCE(SUM(d.plannedMinutes), 0) FROM WorkScheduleDay d WHERE d.scheduleId = :scheduleId")
    Long sumPlannedMinutes(@Param("scheduleId") UUID scheduleId);

}
