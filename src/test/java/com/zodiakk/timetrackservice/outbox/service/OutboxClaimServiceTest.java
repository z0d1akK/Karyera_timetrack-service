package com.zodiakk.timetrackservice.outbox.service;

import com.zodiakk.timetrackservice.outbox.config.OutboxProperties;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEvent;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.timetrackservice.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxClaimServiceTest {

    @Mock
    private OutboxEventRepository repository;

    private OutboxClaimService service;

    @BeforeEach
    void setUp() {
        service = new OutboxClaimService(repository, new OutboxProperties(10, 2000, 180, 15));
    }

    @Test
    void claimBatchMarksProcessing() {
        OutboxEvent event = event();
        when(repository.findAvailableForUpdate(any(), eq(10))).thenReturn(List.of(event));

        assertEquals(List.of(event), service.claimBatch());
        assertEquals(OutboxEventStatus.PROCESSING, event.getStatus());
        assertEquals(1, event.getAttempts());
        verify(repository).flush();
    }

    @Test
    void markPublishedAndFailedDelegateToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.markPublished(eq(id), eq(1), any(), any(), any())).thenReturn(1);
        when(repository.markFailed(eq(id), eq(2), any(), any(), any(), any())).thenReturn(1);

        assertTrue(service.markPublished(id, 1));
        assertTrue(service.markFailed(id, 2));
    }

    private OutboxEvent event() {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .attempts(0)
                .status(OutboxEventStatus.PENDING)
                .build();
    }
}
