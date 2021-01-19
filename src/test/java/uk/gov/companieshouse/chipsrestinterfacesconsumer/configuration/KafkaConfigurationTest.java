package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class KafkaConfigurationTest {

    private static final String INCOMING_GROUP_NAME_VALUE = "incoming-test-group";
    private static final String RETRY_GROUP_NAME_VALUE = "retry-test-group";
    private static final String BROKER_ADDRESS_VALUE = "kafka address";
    private static final String INCOMING_TOPIC_NAME_VALUE = "incoming-topic";
    private static final String RETRY_TOPIC_NAME_VALUE = "retry-topic";
    private static final int POLL_TIMEOUT_VALUE = 100;

    private KafkaConfiguration kafkaConfiguration;

    @BeforeEach
    void setup() {
        kafkaConfiguration = new KafkaConfiguration();
        ReflectionTestUtils.setField(kafkaConfiguration, "incomingConsumerGroupName", INCOMING_GROUP_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "retryConsumerGroupName", RETRY_GROUP_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "brokerAddress", BROKER_ADDRESS_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "incomingTopicName", INCOMING_TOPIC_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "retryTopicName", RETRY_TOPIC_NAME_VALUE);
        ReflectionTestUtils.setField(kafkaConfiguration, "pollTimeout", POLL_TIMEOUT_VALUE);
    }

    @Test
    void getIncomingConsumerConfigTest() {
        ConsumerConfig consumerConfig = kafkaConfiguration.getIncomingConsumerConfig();

        assertEquals(INCOMING_GROUP_NAME_VALUE, consumerConfig.getGroupName());
        assertNotNull(consumerConfig.getBrokerAddresses());
        assertEquals(1, consumerConfig.getBrokerAddresses().length);
        assertEquals(BROKER_ADDRESS_VALUE, consumerConfig.getBrokerAddresses()[0]);
        assertEquals(1, consumerConfig.getTopics().size());
        assertEquals(INCOMING_TOPIC_NAME_VALUE, consumerConfig.getTopics().get(0));
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
        assertEquals(RETRY_TOPIC_NAME_VALUE, consumerConfig.getTopics().get(0));
        assertEquals(100, consumerConfig.getPollTimeout());
    }

    @Test
    void getDeserializerFactory() {
        DeserializerFactory deserializerFactory = kafkaConfiguration.getDeserializerFactory();
        assertNotNull(deserializerFactory);
    }
}
