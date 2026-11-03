package com.zodiakk.timetrackservice.schedule.repository;

import com.zodiakk.timetrackservice.schedule.entity.WorkSchedule;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID>, JpaSpecificationExecutor<WorkSchedule> {

    Optional<WorkSchedule> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    List<WorkSchedule> findAllByActiveTrueOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM WorkSchedule s WHERE s.id = :scheduleId")
    Optional<WorkSchedule> findByIdForUpdate(@Param("scheduleId") UUID scheduleId);

}
