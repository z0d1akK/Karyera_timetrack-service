package com.zodiakk.timetrackservice.outbox.service;

import com.zodiakk.timetrackservice.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private JsonMapper objectMapper = new JsonMapper();

    @InjectMocks
    private OutboxEventService service;

    @Test
    void saveEventStoresEnvelope() throws Exception {
        UUID aggregateId = UUID.randomUUID();
        var method = OutboxEventService.class.getMethod(
                "saveEvent",
                UUID.class,
                String.class,
                String.class,
                int.class,
                Object.class
        );
        assertEquals(Propagation.MANDATORY, method.getAnnotation(
                org.springframework.transaction.annotation.Transactional.class
        ).propagation());

        service.saveEvent(aggregateId, "Timesheet", "TimesheetApproved", 1, Map.of("minutes", 60));

        ArgumentCaptor<com.zodiakk.timetrackservice.outbox.entity.OutboxEvent> captor =
                ArgumentCaptor.forClass(com.zodiakk.timetrackservice.outbox.entity.OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals("TimesheetApproved", captor.getValue().getEventType());
        assertTrue(captor.getValue().getPayload().contains(aggregateId.toString()));
    }
}
