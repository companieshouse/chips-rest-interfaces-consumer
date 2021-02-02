package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class KafkaConfigurationTest {

    private static final String MAIN_GROUP_NAME_VALUE = "main-test-group";
    private static final String RETRY_GROUP_NAME_VALUE = "retry-test-group";
    private static final String ERROR_GROUP_NAME_VALUE = "error-test-group";
    private static final String BROKER_ADDRESS_VALUE = "kafka address";
    private static final String MAIN_TOPIC_NAME_VALUE = "main-topic";
    private static final String RETRY_TOPIC_NAME_VALUE = MAIN_TOPIC_NAME_VALUE + "-cric-retry";
    private static final String ERROR_TOPIC_NAME_VALUE = MAIN_TOPIC_NAME_VALUE + "-cric-error";
    private static final int POLL_TIMEOUT_VALUE = 100;
    private static final int RETRIES = 10;

    private KafkaConfiguration kafkaConfiguration;

    @BeforeEach
    void setup() {
        kafkaConfiguration = new KafkaConfiguration();
        ReflectionTestUtils.setField(kafkaConfiguration, "mainConsumerGroupName", MAIN_GROUP_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "retryConsumerGroupName", RETRY_GROUP_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "errorConsumerGroupName", ERROR_GROUP_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "brokerAddress", BROKER_ADDRESS_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "mainTopicName", MAIN_TOPIC_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "retryTopicName", RETRY_TOPIC_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "errorTopicName", ERROR_TOPIC_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "pollTimeout", POLL_TIMEOUT_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "retries", RETRIES);
    }

    @Test
    void getMainConsumerConfigTest() {
        ConsumerConfig consumerConfig = kafkaConfiguration.getMainConsumerConfig();

        assertEquals(MAIN_GROUP_NAME_VALUE, consumerConfig.getGroupName());
        assertNotNull(consumerConfig.getBrokerAddresses());
        assertEquals(1, consumerConfig.getBrokerAddresses().length);
        assertEquals(BROKER_ADDRESS_VALUE, consumerConfig.getBrokerAddresses()[0]);
        assertEquals(1, consumerConfig.getTopics().size());
        assertEquals(MAIN_TOPIC_NAME_VALUE, consumerConfig.getTopics().get(0));
        assertEquals(100, consumerConfig.getPollTimeout());
    }

    @Test
    void getRetryConsumerConfigTest() {
        ConsumerConfig consumerConfig = kafkaConfiguration.getRetryConsumerConfig();

        assertEquals(RETRY_GROUP_NAME_VALUE, consumerConfig.getGroupName());
        assertNotNull(consumerConfig.getBrokerAddresses());
        assertEquals(1, consumerConfig.getBrokerAddresses().length);
        assertEquals(BROKER_ADDRESS_VALUE, consumerConfig.getBrokerAddresses()[0]);
        assertEquals(1, consumerConfig.getTopics().size());
        // Retry topic calculates name based off of main topic
        assertEquals(RETRY_TOPIC_NAME_VALUE, consumerConfig.retryTopic());
        assertEquals(100, consumerConfig.getPollTimeout());
    }

    @Test
    void getErrorConsumerConfigTest() {
        ConsumerConfig consumerConfig = kafkaConfiguration.getErrorConsumerConfig();

        assertEquals(ERROR_GROUP_NAME_VALUE, consumerConfig.getGroupName());
        assertNotNull(consumerConfig.getBrokerAddresses());
        assertEquals(1, consumerConfig.getBrokerAddresses().length);
        assertEquals(BROKER_ADDRESS_VALUE, consumerConfig.getBrokerAddresses()[0]);
        assertEquals(1, consumerConfig.getTopics().size());
        // Retry topic calculates name based off of main topic
        assertEquals(ERROR_TOPIC_NAME_VALUE, consumerConfig.errorTopic());
        assertEquals(100, consumerConfig.getPollTimeout());
    }

    @Test
    void getMessageProducerConfigTest() {
        ProducerConfig producerConfig = kafkaConfiguration.getMessageProducerConfig();

        assertNotNull(producerConfig.getBrokerAddresses());
        assertEquals(1, producerConfig.getBrokerAddresses().length);
        assertEquals(BROKER_ADDRESS_VALUE, producerConfig.getBrokerAddresses()[0]);
        assertTrue(producerConfig.isRoundRobinPartitioner());
        assertEquals(Acks.WAIT_FOR_ALL, producerConfig.getAcks());
        assertEquals(RETRIES, producerConfig.getRetries());
    }

    @Test
    void getDeserializerFactory() {
        DeserializerFactory deserializerFactory = kafkaConfiguration.getDeserializerFactory();
        assertNotNull(deserializerFactory);
    }

    @Test
    void getSerializerFactory() {
        SerializerFactory serializerFactory = kafkaConfiguration.getSerializerFactory();
        assertNotNull(serializerFactory);
    }
}
