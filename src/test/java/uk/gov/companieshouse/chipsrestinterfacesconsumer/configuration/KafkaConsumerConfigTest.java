package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaConsumerConfigTest {

    private static final String BROKER_ADDRESS = "BROKER";
    private static final long RETRY_THROTTLE_SECONDS = 5000L;
    private static final int MAX_RETRY_ATTEMPTS = 5;

    private KafkaConsumerConfig kafkaConsumerConfig;

    @BeforeEach
    void init() {
        kafkaConsumerConfig = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(kafkaConsumerConfig, "brokerAddress", BROKER_ADDRESS);
        ReflectionTestUtils.setField(kafkaConsumerConfig, "retryThrottleSeconds", RETRY_THROTTLE_SECONDS);
        ReflectionTestUtils.setField(kafkaConsumerConfig, "maxRetryAttempts", MAX_RETRY_ATTEMPTS);
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
    void kafkaRetryListenerContainerFactoryRetryLessThanDefaultPollInterval() {
        var retryInterval = 1L;
        ReflectionTestUtils.setField(kafkaConsumerConfig, "retryThrottleSeconds", retryInterval);
        var factory = kafkaConsumerConfig.kafkaRetryListenerContainerFactory();
        assertEquals(retryInterval * 1000L, factory.getContainerProperties().getIdleBetweenPolls());
        assertEquals(300000, factory.getConsumerFactory().getConfigurationProperties().get(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    }
}