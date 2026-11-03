package com.zodiakk.timetrackservice.outbox.publisher;

import com.zodiakk.timetrackservice.outbox.config.OutboxProperties;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEvent;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.timetrackservice.outbox.service.OutboxClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherTest {

    @Mock
    private OutboxClaimService claimService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new OutboxPublisher(claimService, kafkaTemplate, new OutboxProperties(10, 2000, 180, 15));
        ReflectionTestUtils.setField(publisher, "timeEventsTopic", "time.events.v1");
    }

    @Test
    void publishesClaimedEvent() {
        OutboxEvent event = event();
        when(claimService.claimBatch()).thenReturn(List.of(event));
        when(kafkaTemplate.send("time.events.v1", event.getAggregateId().toString(), "{}"))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(claimService.markPublished(event.getId(), 1)).thenReturn(true);

        publisher.publishPendingEvents();

        verify(claimService).markPublished(event.getId(), 1);
    }

    @Test
    void marksFailedWhenSendThrows() {
        OutboxEvent event = event();
        when(claimService.claimBatch()).thenReturn(List.of(event));
        when(kafkaTemplate.send("time.events.v1", event.getAggregateId().toString(), "{}"))
                .thenThrow(new IllegalStateException("kafka"));

        publisher.publishPendingEvents();

        verify(claimService).markFailed(event.getId(), 1);
        verify(claimService, never()).markPublished(event.getId(), 1);
    }

    private OutboxEvent event() {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .attempts(1)
                .status(OutboxEventStatus.PROCESSING)
                .payload("{}")
                .eventType("TimesheetApproved")
                .build();
    }
}
