package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaProducerConfigTest {

    private static final String BROKER_ADDRESS = "BROKER";
    private static final int KAFKA_PRODUCER_BATCH_SIZE_BYTES = 131072;
    private static final int KAFKA_PRODUCER_LINGER_MS = 500;

    private KafkaProducerConfig kafkaProducerConfig;

    @BeforeEach
    void init() {
        kafkaProducerConfig = new KafkaProducerConfig();
        ReflectionTestUtils.setField(kafkaProducerConfig, "brokerAddress", BROKER_ADDRESS);
        ReflectionTestUtils.setField(kafkaProducerConfig, "batchSizeBytes", KAFKA_PRODUCER_BATCH_SIZE_BYTES);
        ReflectionTestUtils.setField(kafkaProducerConfig, "lingerMs", KAFKA_PRODUCER_LINGER_MS);
    }

    @Test
    void testKafkaConfigValues() {
        var factory = kafkaProducerConfig.producerFactory();
        assertEquals(BROKER_ADDRESS, factory.getConfigurationProperties().get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals(KAFKA_PRODUCER_BATCH_SIZE_BYTES, factory.getConfigurationProperties().get(ProducerConfig.BATCH_SIZE_CONFIG));
        assertEquals(KAFKA_PRODUCER_LINGER_MS, factory.getConfigurationProperties().get(ProducerConfig.LINGER_MS_CONFIG));
    }
}
