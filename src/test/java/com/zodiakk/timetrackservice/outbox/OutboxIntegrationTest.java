package com.zodiakk.timetrackservice.outbox;

import com.zodiakk.timetrackservice.common.AbstractIntegrationTest;
import com.zodiakk.timetrackservice.common.constants.EventConstants;
import com.zodiakk.timetrackservice.housekeeping.HousekeepingService;
import com.zodiakk.timetrackservice.integration.repository.ProcessedEventRepository;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEvent;
import com.zodiakk.timetrackservice.outbox.entity.OutboxEventStatus;
import com.zodiakk.timetrackservice.outbox.publisher.OutboxPublisher;
import com.zodiakk.timetrackservice.outbox.repository.OutboxEventRepository;
import com.zodiakk.timetrackservice.outbox.service.OutboxClaimService;
import com.zodiakk.timetrackservice.outbox.service.OutboxEventService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Transactional outbox")
class OutboxIntegrationTest extends AbstractIntegrationTest {

    private static final String TOPIC = "time.events.v1";

    @Autowired
    private OutboxEventService outboxEventService;

    @Autowired
    private OutboxClaimService outboxClaimService;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private HousekeepingService housekeepingService;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    @DisplayName("publishes a pending event and does not publish it twice")
    void shouldPublishOnce() {
        UUID aggregateId = saveEvent();

        outboxPublisher.publishPendingEvents();
        awaitStatus(OutboxEventStatus.PUBLISHED, Duration.ofSeconds(20));

        List<ConsumerRecord<String, String>> records = pollRecords(aggregateId, Duration.ofSeconds(20));
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().value()).contains("TimesheetSubmitted");

        outboxPublisher.publishPendingEvents();
        assertThat(outboxEventRepository.findAll().getFirst().getAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("schedules a retry and skips an event that is not due")
    void shouldDelayRetry() {
        saveEvent();
        OutboxEvent claimed = outboxClaimService.claimBatch().getFirst();

        assertThat(outboxClaimService.markFailed(claimed.getId(), claimed.getAttempts())).isTrue();
        assertThat(outboxClaimService.claimBatch()).isEmpty();
    }

    @Test
    @DisplayName("removes published events past retention")
    void shouldHousekeep() {
        OutboxEvent expired = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType(EventConstants.AGGREGATE_TIMESHEET)
                .eventType(EventConstants.TIMESHEET_APPROVED)
                .eventVersion(1)
                .payload("{}")
                .status(OutboxEventStatus.PUBLISHED)
                .attempts(1)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(40))
                .publishedAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(40))
                .nextAttemptAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(40))
                .build();
        outboxEventRepository.saveAndFlush(expired);
        jdbcTemplate.update(
                "INSERT INTO processed_events (event_id, processed_at) VALUES (?, ?)",
                UUID.randomUUID(),
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(60)
        );

        housekeepingService.cleanup();

        assertThat(outboxEventRepository.findAll()).isEmpty();
        assertThat(processedEventRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("refuses to record an event outside a transaction")
    void shouldRequireTransaction() {
        assertThatThrownBy(() -> outboxEventService.saveEvent(
                UUID.randomUUID(),
                EventConstants.AGGREGATE_TIMESHEET,
                EventConstants.TIMESHEET_SUBMITTED,
                1,
                Map.of("minutes", 1)
        )).isInstanceOf(IllegalTransactionStateException.class);
    }

    private UUID saveEvent() {
        UUID aggregateId = UUID.randomUUID();
        transactionTemplate.executeWithoutResult(status -> outboxEventService.saveEvent(
                aggregateId,
                EventConstants.AGGREGATE_TIMESHEET,
                EventConstants.TIMESHEET_SUBMITTED,
                EventConstants.DEFAULT_EVENT_VERSION,
                Map.of("timesheetId", aggregateId.toString())
        ));
        return aggregateId;
    }

    private void awaitStatus(OutboxEventStatus expected, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            List<OutboxEvent> events = outboxEventRepository.findAll();
            if (!events.isEmpty() && events.stream().allMatch(event -> event.getStatus() == expected)) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        throw new AssertionError("Outbox status was not " + expected);
    }

    private List<ConsumerRecord<String, String>> pollRecords(UUID aggregateId, Duration timeout) {
        List<ConsumerRecord<String, String>> matching = new ArrayList<>();
        try (Consumer<String, String> consumer = consumerFactory.createConsumer("timetrack-outbox-" + UUID.randomUUID())) {
            consumer.subscribe(List.of(TOPIC));
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline && matching.isEmpty()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                records.forEach(record -> {
                    if (aggregateId.toString().equals(record.key())) {
                        matching.add(record);
                    }
                });
            }
        }
        return matching;
    }
}
