package com.zodiakk.timetrackservice;

import com.zodiakk.timetrackservice.common.AbstractIntegrationTest;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Time tracking API")
class TimetrackApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("creates a schedule, logs time and approves the weekly timesheet")
    void shouldRunWeeklyFlow() throws Exception {
        UUID employeeUserId = UUID.randomUUID();
        UUID managerUserId = UUID.randomUUID();
        EmployeeLookupItem employee = registerEmployee(employeeUserId);
        EmployeeLookupItem manager = registerEmployee(managerUserId);
        assignManager(manager.id(), employee.id());

        String scheduleId = createSchedule();
        mockMvc.perform(asHr(post("/api/v1/time/employees/{id}/work-schedules", employee.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduleId":"%s","validFrom":"2026-10-01"}
                                """.formatted(scheduleId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleName").isNotEmpty());

        mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {
                                  "projectId":"11111111-1111-4111-8111-111111111111",
                                  "taskId":"22222222-2222-4222-8222-222222222222",
                                  "workDate":"2026-10-19",
                                  "durationMinutes":240,
                                  "description":"API"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));

        MvcResult created = mockMvc.perform(asEmployee(post("/api/v1/time/timesheets")
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {"periodStart":"2026-10-19","periodEnd":"2026-10-25"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plannedMinutes").value(480))
                .andReturn();

        String timesheetId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asString();
        long version = objectMapper.readTree(created.getResponse().getContentAsString()).get("version").asLong();

        mockMvc.perform(asEmployee(post("/api/v1/time/timesheets/{id}/submit", timesheetId)
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("{\"expectedVersion\":" + version + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timesheet.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.warnings").isArray());

        MvcResult loaded = mockMvc.perform(asEmployee(get("/api/v1/time/timesheets/{id}", timesheetId), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn();
        long submittedVersion = objectMapper.readTree(loaded.getResponse().getContentAsString()).get("version").asLong();

        mockMvc.perform(asEmployee(post("/api/v1/time/timesheets/{id}/approve", timesheetId)
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {"expectedVersion":%d,"comment":"сам"}
                                """.formatted(submittedVersion)))
                .andExpect(status().isForbidden());

        mockMvc.perform(asManager(post("/api/v1/time/timesheets/{id}/approve", timesheetId)
                        .contentType(MediaType.APPLICATION_JSON), managerUserId)
                        .content("""
                                {"expectedVersion":%d,"comment":"ок"}
                                """.formatted(submittedVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(asInternal(get("/internal/v1/time/approved-entries")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].durationMinutes").value(240));

        mockMvc.perform(asInternal(get("/internal/v1/time/employees/{id}/approved-summary", employee.id())
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApprovedMinutes").value(240));

        mockMvc.perform(asEmployee(get("/api/v1/time/employees/{id}/workload", employee.id())
                        .param("periodStart", "2026-10-19")
                        .param("periodEnd", "2026-10-25"), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvedMinutes").value(240));

        mockMvc.perform(asManager(get("/api/v1/time/teams/workload")
                        .param("periodStart", "2026-10-19")
                        .param("periodEnd", "2026-10-25"), managerUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employees[0].employeeId").value(employee.id().toString()));
    }

    @Test
    @DisplayName("rejects a second entry that would exceed the daily limit")
    void shouldRejectDailyOverflow() throws Exception {
        UUID userId = UUID.randomUUID();
        registerEmployee(userId);
        when(projectInternalClient.getWorkEligibility(any(), any(), any(), any())).thenAnswer(invocation ->
                new WorkEligibilityDto(
                        true,
                        invocation.getArgument(0),
                        invocation.getArgument(2),
                        invocation.getArgument(1),
                        invocation.getArgument(3),
                        UUID.randomUUID(),
                        true,
                        true,
                        true,
                        false,
                        null
                ));

        createEntry(userId, 1000);
        mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), userId)
                        .content(entryBody(500)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("rejects time when the project says the employee is not eligible")
    void shouldRejectIneligibleWork() throws Exception {
        UUID userId = UUID.randomUUID();
        registerEmployee(userId);
        when(projectInternalClient.getWorkEligibility(any(), any(), any(), any())).thenReturn(
                new WorkEligibilityDto(
                        false,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        LocalDate.of(2026, 10, 19),
                        null,
                        false,
                        true,
                        false,
                        false,
                        "NO_ACTIVE_PROJECT_ASSIGNMENT"
                ));

        mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), userId)
                        .content(entryBody(60)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("rejects a timesheet that is not a Monday-Sunday week")
    void shouldRejectNonWeekPeriod() throws Exception {
        UUID userId = UUID.randomUUID();
        registerEmployee(userId);

        mockMvc.perform(asEmployee(post("/api/v1/time/timesheets")
                        .contentType(MediaType.APPLICATION_JSON), userId)
                        .content("""
                                {"periodStart":"2026-10-20","periodEnd":"2026-10-26"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("lists schedules and entries with filters")
    void shouldFilterCollections() throws Exception {
        createSchedule();
        mockMvc.perform(asAdmin(get("/api/v1/time/work-schedules").param("code", "FLEX")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        UUID userId = UUID.randomUUID();
        registerEmployee(userId);
        createEntry(userId, 30);
        mockMvc.perform(asEmployee(get("/api/v1/time/entries")
                        .param("workDateFrom", "2026-10-19")
                        .param("workDateTo", "2026-10-19")
                        .param("sortBy", "workDate"), userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].durationMinutes").value(30));
    }

    @Test
    @DisplayName("rejects overlapping schedule assignments")
    void shouldRejectOverlappingAssignment() throws Exception {
        UUID userId = UUID.randomUUID();
        EmployeeLookupItem employee = registerEmployee(userId);
        String scheduleId = createSchedule();
        String body = """
                {"scheduleId":"%s","validFrom":"2026-11-01"}
                """.formatted(scheduleId);

        mockMvc.perform(asHr(post("/api/v1/time/employees/{id}/work-schedules", employee.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(asHr(post("/api/v1/time/employees/{id}/work-schedules", employee.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("returns a calendar and rejects a schedule day change after assignment")
    void shouldProtectAssignedScheduleDays() throws Exception {
        UUID userId = UUID.randomUUID();
        EmployeeLookupItem employee = registerEmployee(userId);
        String scheduleId = createSchedule();
        mockMvc.perform(asHr(post("/api/v1/time/employees/{id}/work-schedules", employee.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"scheduleId":"%s","validFrom":"2026-10-01"}
                                """.formatted(scheduleId))))
                .andExpect(status().isCreated());

        mockMvc.perform(asEmployee(get("/api/v1/time/employees/{id}/work-calendar", employee.id())
                        .param("from", "2026-10-19")
                        .param("to", "2026-10-20"), userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedMinutes").value(480));

        mockMvc.perform(asHr(patch("/api/v1/time/work-schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"days":[{"dayOfWeek":1,"plannedMinutes":120}]}
                                """)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("updates and voids a draft, then rejects a submitted timesheet")
    void shouldUpdateVoidAndReject() throws Exception {
        UUID employeeUserId = UUID.randomUUID();
        UUID managerUserId = UUID.randomUUID();
        EmployeeLookupItem employee = registerEmployee(employeeUserId);
        EmployeeLookupItem manager = registerEmployee(managerUserId);
        assignManager(manager.id(), employee.id());

        MvcResult created = mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content(entryBody(90)))
                .andExpect(status().isCreated())
                .andReturn();
        String entryId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(asEmployee(patch("/api/v1/time/entries/{id}", entryId)
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {"durationMinutes":120,"description":"уточнение"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(120));

        mockMvc.perform(asEmployee(get("/api/v1/time/entries/{id}/audit", entryId), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").exists());

        mockMvc.perform(asEmployee(post("/api/v1/time/entries/{id}/void", entryId)
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {"reason":"ошибочная запись","expectedVersion":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOIDED"));

        mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content(entryBody(60)))
                .andExpect(status().isCreated());

        MvcResult timesheet = mockMvc.perform(asEmployee(post("/api/v1/time/timesheets")
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("""
                                {"periodStart":"2026-10-19","periodEnd":"2026-10-25"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String timesheetId = objectMapper.readTree(timesheet.getResponse().getContentAsString()).get("id").asString();
        long version = objectMapper.readTree(timesheet.getResponse().getContentAsString()).get("version").asLong();

        mockMvc.perform(asEmployee(post("/api/v1/time/timesheets/{id}/submit", timesheetId)
                        .contentType(MediaType.APPLICATION_JSON), employeeUserId)
                        .content("{\"expectedVersion\":" + version + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(asManager(get("/api/v1/time/timesheets/pending-approval"), managerUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(timesheetId));

        MvcResult loaded = mockMvc.perform(asManager(get("/api/v1/time/timesheets/{id}", timesheetId), managerUserId))
                .andExpect(status().isOk())
                .andReturn();
        long submittedVersion = objectMapper.readTree(loaded.getResponse().getContentAsString()).get("version").asLong();

        mockMvc.perform(asManager(post("/api/v1/time/timesheets/{id}/reject", timesheetId)
                        .contentType(MediaType.APPLICATION_JSON), managerUserId)
                        .content("""
                                {"expectedVersion":%d,"comment":"нужны детали"}
                                """.formatted(submittedVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mockMvc.perform(asEmployee(get("/api/v1/time/timesheets/{id}/reviews", timesheetId), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].decision").value("REJECT"));

        mockMvc.perform(asEmployee(get("/api/v1/time/employees/{id}/daily-summary", employee.id())
                        .param("from", "2026-10-19")
                        .param("to", "2026-10-19"), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].rejectedMinutes").value(60));

        mockMvc.perform(asEmployee(get("/api/v1/time/employees/{id}/weekly-summary", employee.id())
                        .param("weekStart", "2026-10-19"), employeeUserId))
                .andExpect(status().isOk());

        mockMvc.perform(asManager(get("/api/v1/time/projects/{id}/time-summary", "11111111-1111-4111-8111-111111111111")
                        .param("periodStart", "2026-10-19")
                        .param("periodEnd", "2026-10-25"), managerUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draftMinutes").value(60));

        mockMvc.perform(asHr(get("/api/v1/time/timesheets")
                        .param("employeeId", employee.id().toString())
                        .param("status", "REJECTED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("REJECTED"));

        mockMvc.perform(asEmployee(get("/api/v1/time/employees/{id}/timesheets", employee.id()), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(timesheetId));

        mockMvc.perform(asHr(get("/api/v1/time/teams/workload")
                        .param("periodStart", "2026-10-19")
                        .param("periodEnd", "2026-10-25")))
                .andExpect(status().isOk());

        mockMvc.perform(asEmployee(get("/api/v1/time/entries/{id}", entryId), employeeUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VOIDED"));
    }

    private String createSchedule() throws Exception {
        MvcResult result = mockMvc.perform(asHr(post("/api/v1/time/work-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code":"FLEX_%s",
                                  "name":"Flexible",
                                  "timezone":"Europe/Vilnius",
                                  "days":[
                                    {"dayOfWeek":1,"plannedMinutes":480,"startTime":"09:00","endTime":"18:00"}
                                  ]
                                }
                                """.formatted(UUID.randomUUID().toString().substring(0, 8)))))
                .andExpect(status().isCreated())
                .andReturn();
        String scheduleId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(asHr(patch("/api/v1/time/work-schedules/{id}", scheduleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated","timezone":"Europe/Warsaw","active":true,"code":"UPD_%s"}
                                """.formatted(UUID.randomUUID().toString().substring(0, 8)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("Europe/Warsaw"));

        mockMvc.perform(asHr(get("/api/v1/time/work-schedules/{id}", scheduleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].plannedMinutes").value(480));

        return scheduleId;
    }

    private void createEntry(UUID userId, int minutes) throws Exception {
        mockMvc.perform(asEmployee(post("/api/v1/time/entries")
                        .contentType(MediaType.APPLICATION_JSON), userId)
                        .content(entryBody(minutes)))
                .andExpect(status().isCreated());
    }

    private String entryBody(int minutes) {
        return """
                {
                  "projectId":"11111111-1111-4111-8111-111111111111",
                  "taskId":"22222222-2222-4222-8222-222222222222",
                  "workDate":"2026-10-19",
                  "durationMinutes":%d,
                  "description":"API"
                }
                """.formatted(minutes);
    }
}
