package com.zodiakk.timetrackservice.timeentry.repository;

import com.zodiakk.timetrackservice.timeentry.entity.TimeEntryAudit;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TimeEntryAuditRepository extends JpaRepository<TimeEntryAudit, UUID>, JpaSpecificationExecutor<TimeEntryAudit> {

    List<TimeEntryAudit> findAllByTimeEntryIdOrderByCreatedAtDesc(UUID timeEntryId);

    List<TimeEntryAudit> findAllByActorEmployeeId(UUID actorEmployeeId);

}
