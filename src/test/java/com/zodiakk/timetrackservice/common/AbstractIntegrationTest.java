package com.zodiakk.timetrackservice.common;

import com.zodiakk.timetrackservice.TestcontainersConfiguration;
import com.zodiakk.timetrackservice.common.cache.CacheNames;
import com.zodiakk.timetrackservice.common.constants.Headers;
import com.zodiakk.timetrackservice.integration.employee.client.EmployeeInternalClient;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeAccessScopeDto;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupItem;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupRequest;
import com.zodiakk.timetrackservice.integration.employee.dto.EmployeeLookupResponse;
import com.zodiakk.timetrackservice.integration.employee.dto.SubordinateIdsDto;
import com.zodiakk.timetrackservice.integration.project.client.ProjectInternalClient;
import com.zodiakk.timetrackservice.integration.project.dto.WorkEligibilityDto;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final String USER_ID_HEADER = Headers.USER_ID;

    protected static final String USER_ROLE_HEADER = Headers.USER_ROLE;

    protected static final String USER_EMAIL_HEADER = Headers.USER_EMAIL;

    protected static final String INTERNAL_KEY_HEADER = Headers.INTERNAL_KEY;

    protected static final String INTERNAL_API_KEY = "test-internal-api-key";

    private final Map<UUID, EmployeeLookupItem> employeesById = new HashMap<>();

    private final Map<UUID, EmployeeLookupItem> employeesByUserId = new HashMap<>();

    private final Map<UUID, List<UUID>> subordinatesByManager = new HashMap<>();

    private final Map<UUID, UUID> managerByEmployee = new HashMap<>();

    @MockitoBean
    protected EmployeeInternalClient employeeInternalClient;

    @MockitoBean
    protected ProjectInternalClient projectInternalClient;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected CacheManager cacheManager;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> TestcontainersConfiguration.kafka().getBootstrapServers()
        );
    }

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("""
                TRUNCATE TABLE time_entry_audit, time_entries, timesheet_reviews, timesheets,
                    employee_work_schedules, work_schedule_days, work_schedules,
                    processed_events, outbox_events
                    CASCADE
                """);

        Cache schedules = cacheManager.getCache(CacheNames.WORK_SCHEDULES);
        if (schedules != null) {
            schedules.clear();
        }

        employeesById.clear();
        employeesByUserId.clear();
        subordinatesByManager.clear();
        managerByEmployee.clear();
        stubEmployeeClient();
        stubProjectClient();
    }

    protected String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    protected EmployeeLookupItem registerEmployee(UUID userId) {
        EmployeeLookupItem item = new EmployeeLookupItem(
                UUID.randomUUID(),
                "Ivan Petrov",
                true,
                null,
                null
        );
        employeesById.put(item.id(), item);
        if (userId != null) {
            employeesByUserId.put(userId, item);
        }
        return item;
    }

    protected void assignManager(UUID managerEmployeeId, UUID employeeId) {
        managerByEmployee.put(employeeId, managerEmployeeId);
        subordinatesByManager.computeIfAbsent(managerEmployeeId, ignored -> new ArrayList<>()).add(employeeId);
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asUser(
            B builder,
            UUID userId,
            String role
    ) {
        return builder
                .header(USER_ID_HEADER, userId.toString())
                .header(USER_ROLE_HEADER, role)
                .header(USER_EMAIL_HEADER, "user-" + userId + "@company.com");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asHr(B builder) {
        return asUser(builder, UUID.randomUUID(), "HR");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asAdmin(B builder) {
        return asUser(builder, UUID.randomUUID(), "ADMIN");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asEmployee(B builder, UUID userId) {
        return asUser(builder, userId, "EMPLOYEE");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asManager(B builder, UUID userId) {
        return asUser(builder, userId, "MANAGER");
    }

    protected <B extends AbstractMockHttpServletRequestBuilder<B>> B asInternal(B builder) {
        return builder.header(INTERNAL_KEY_HEADER, INTERNAL_API_KEY);
    }

    private void stubEmployeeClient() {
        lenient().when(employeeInternalClient.lookup(any(EmployeeLookupRequest.class)))
                .thenAnswer(invocation -> {
                    EmployeeLookupRequest request = invocation.getArgument(0);
                    List<EmployeeLookupItem> found = request.employeeIds() == null
                            ? List.of()
                            : request.employeeIds().stream()
                            .filter(employeesById::containsKey)
                            .map(employeesById::get)
                            .toList();
                    return new EmployeeLookupResponse(found);
                });

        lenient().when(employeeInternalClient.getByUserId(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID userId = invocation.getArgument(0);
                    EmployeeLookupItem item = employeesByUserId.get(userId);
                    if (item == null) {
                        throw notFound("/internal/v1/employees/by-user/" + userId);
                    }
                    return item;
                });

        lenient().when(employeeInternalClient.getAccessScope(any(UUID.class), any()))
                .thenAnswer(invocation -> {
                    UUID employeeId = invocation.getArgument(0);
                    UUID actorEmployeeId = invocation.getArgument(1);
                    boolean self = employeeId != null && employeeId.equals(actorEmployeeId);
                    boolean manager = actorEmployeeId != null
                            && actorEmployeeId.equals(managerByEmployee.get(employeeId));
                    return new EmployeeAccessScopeDto(employeeId, actorEmployeeId, self, manager && !self, true);
                });

        lenient().when(employeeInternalClient.getSubordinateIds(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID managerId = invocation.getArgument(0);
                    if (!employeesById.containsKey(managerId)) {
                        throw notFound("/internal/v1/employees/" + managerId + "/subordinate-ids");
                    }
                    return new SubordinateIdsDto(
                            managerId,
                            List.copyOf(subordinatesByManager.getOrDefault(managerId, List.of()))
                    );
                });
    }

    private void stubProjectClient() {
        lenient().when(projectInternalClient.getWorkEligibility(any(), any(), any(), any()))
                .thenAnswer(invocation -> {
                    UUID projectId = invocation.getArgument(0);
                    UUID employeeId = invocation.getArgument(1);
                    UUID taskId = invocation.getArgument(2);
                    LocalDate workDate = invocation.getArgument(3);
                    return new WorkEligibilityDto(
                            true,
                            projectId,
                            taskId,
                            employeeId,
                            workDate,
                            UUID.randomUUID(),
                            true,
                            true,
                            true,
                            false,
                            null
                    );
                });
    }

    private FeignException.NotFound notFound(String path) {
        return new FeignException.NotFound(
                "missing",
                Request.create(
                        Request.HttpMethod.GET,
                        path,
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null
                ),
                null,
                Map.of()
        );
    }
}
