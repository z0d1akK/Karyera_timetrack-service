package com.zodiakk.timetrackservice.housekeeping;

import com.zodiakk.timetrackservice.integration.repository.ProcessedEventRepository;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.timetrackservice.outbox.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HousekeepingServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private HousekeepingService service;

    @Test
    void cleanupDeletesExpiredRows() {
        ReflectionTestUtils.setField(service, "outboxRetentionDays", 7L);
        ReflectionTestUtils.setField(service, "processedEventsRetentionDays", 30L);
        when(outboxEventRepository.deletePublishedBefore(eq(OutboxEventStatus.PUBLISHED), any())).thenReturn(2);
        when(processedEventRepository.deleteProcessedBefore(any())).thenReturn(1);

        service.cleanup();

        verify(outboxEventRepository).deletePublishedBefore(eq(OutboxEventStatus.PUBLISHED), any());
        verify(processedEventRepository).deleteProcessedBefore(any());
    }
}
