package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerConfigTest {

    private static final String BROKER_ADDRESS = "BROKER";
    private static final long RETRY_THROTTLE_SECONDS = 5000L;
    private static final Long TIMESTAMP_PAST = 1614693284647L;
    private static final Long TIMESTAMP_NOW = 1614693285647L;
    private static final Long TIMESTAMP_FUTURE = 1614693286647L;

    @Mock
    private ConsumerRecord<String, ChipsRestInterfacesSend> mockConsumerRecord;

    @Mock
    private Supplier<Long> mockTimestampNow;

    @InjectMocks
    private KafkaConsumerConfig kafkaConsumerConfig;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(kafkaConsumerConfig, "brokerAddress", BROKER_ADDRESS);
        ReflectionTestUtils.setField(kafkaConsumerConfig, "retryThrottleSeconds", RETRY_THROTTLE_SECONDS);
    }

    @Test
    void kafkaListenerContainerFactory() {
        var factory = kafkaConsumerConfig.kafkaListenerContainerFactory();
        assertEquals(0, factory.getContainerProperties().getIdleBetweenPolls());
    }

    @Test
    void kafkaRetryListenerContainerFactory() {
        var factory = kafkaConsumerConfig.kafkaRetryListenerContainerFactory();
        assertEquals(RETRY_THROTTLE_SECONDS * 1000L, factory.getContainerProperties().getIdleBetweenPolls());
        assertEquals((int) RETRY_THROTTLE_SECONDS * 1000, factory.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    }

    @Test
    void kafkaErrorListenerContainerFactory() {
        var factory = kafkaConsumerConfig.kafkaErrorListenerContainerFactory();
        assertEquals(0, factory.getContainerProperties().getIdleBetweenPolls());
    }

    @Test
    void kafkaRetryListenerContainerFactoryRetryLessThanDefaultPollInterval() {
        var retryInterval = 1L;
        ReflectionTestUtils.setField(kafkaConsumerConfig, "retryThrottleSeconds", retryInterval);
        var factory = kafkaConsumerConfig.kafkaRetryListenerContainerFactory();
        assertEquals(retryInterval * 1000L, factory.getContainerProperties().getIdleBetweenPolls());
        assertEquals(300000, factory.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    }

    @Test
    void kafkaErrorListenerContainerFactoryDoesFilterNewRecords() {
        when(mockTimestampNow.get()).thenReturn(TIMESTAMP_NOW);
        var factory = kafkaConsumerConfig.kafkaErrorListenerContainerFactory();
        @SuppressWarnings("unchecked")
        var recordFilterStrategy = (RecordFilterStrategy<String, ChipsRestInterfacesSend>) ReflectionTestUtils.getField(factory, "recordFilterStrategy");
        when(mockConsumerRecord.timestamp()).thenReturn(TIMESTAMP_FUTURE);

        assertNotNull(recordFilterStrategy);

        boolean isFiltered = recordFilterStrategy.filter(mockConsumerRecord);

        assertTrue(isFiltered);
    }

    @Test
    void kafkaErrorListenerContainerFactoryDoesNotFilterRecordsCreatedAtTheExactSameTimeTheAppStarted() {
        when(mockTimestampNow.get()).thenReturn(TIMESTAMP_NOW);
        var factory = kafkaConsumerConfig.kafkaErrorListenerContainerFactory();
        @SuppressWarnings("unchecked")
        var recordFilterStrategy = (RecordFilterStrategy<String, ChipsRestInterfacesSend>) ReflectionTestUtils.getField(factory, "recordFilterStrategy");
        when(mockConsumerRecord.timestamp()).thenReturn(TIMESTAMP_NOW);

        assertNotNull(recordFilterStrategy);

        boolean isFiltered = recordFilterStrategy.filter(mockConsumerRecord);

        assertFalse(isFiltered);
    }

    @Test
    void kafkaErrorListenerContainerFactoryDoesNotFilterOldRecords() {
        when(mockTimestampNow.get()).thenReturn(TIMESTAMP_NOW);
        var factory = kafkaConsumerConfig.kafkaErrorListenerContainerFactory();
        @SuppressWarnings("unchecked")
        var recordFilterStrategy = (RecordFilterStrategy<String, ChipsRestInterfacesSend>) ReflectionTestUtils.getField(factory, "recordFilterStrategy");
        when(mockConsumerRecord.timestamp()).thenReturn(TIMESTAMP_PAST);

        assertNotNull(recordFilterStrategy);

        boolean isFiltered = recordFilterStrategy.filter(mockConsumerRecord);

        assertFalse(isFiltered);
    }
}