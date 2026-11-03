package com.zodiakk.timetrackservice.timesheet.repository;

import com.zodiakk.timetrackservice.timesheet.entity.TimesheetReview;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TimesheetReviewRepository extends JpaRepository<TimesheetReview, UUID>, JpaSpecificationExecutor<TimesheetReview> {

    List<TimesheetReview> findAllByTimesheetIdOrderByReviewedAtDesc(UUID timesheetId);

}
