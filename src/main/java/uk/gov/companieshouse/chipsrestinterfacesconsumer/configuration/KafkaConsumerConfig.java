package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.avro.AvroDeserializer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final int MINIMUM_POLL_INTERVAL = 300000;

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleSeconds;

    @Value("${MAX_RETRY_ATTEMPTS}")
    private int maxRetryAttempts;

    /**
     *
     * @return A Map of default Configs for ConsumerFactories to use as a starting point.
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                brokerAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "CRIC-Group");
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                AvroDeserializer.class);
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");
        return props;
    }

    /**
     *
     * @return A consumer factory with no delay between polls
     */
    private ConsumerFactory<String, ChipsRestInterfacesSend> newMainConsumerFactory() {
        var props = getDefaultConfig();
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new AvroDeserializer<>(ChipsRestInterfacesSend.class));
    }

    /**
     *
     * @return A consumer factory with delay between polls
     */
    private ConsumerFactory<String, ChipsRestInterfacesSend> newRetryConsumerFactory() {
        var maxPollInterval = Math.max(MINIMUM_POLL_INTERVAL, (int) retryThrottleSeconds * 1000);

        var props = getDefaultConfig();
        props.put(
                ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                maxPollInterval
        );

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new AvroDeserializer<>(ChipsRestInterfacesSend.class));
    }

    /**
     * Creates a concurrent kafka listener container factory.
     * For each message a kafka listener receives this factory will create a container to process the message.
     *
     * @return A ConcurrentKafkaListenerContainerFactory
     */
    private ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> getNewDefaultContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(newMainConsumerFactory());
        return factory;
    }

    /**
     * Creates a concurrent kafka listener container factory.
     * For each message a kafka listener receives this factory will create a container to process the message.
     * Batch is set to true so that it consumes all the messages after the delay period. Without this it will
     * consume one message per poll
     *
     * @param idleMillis the amount of time in millis you want the consumer to wait between polls
     * @return A ConcurrentKafkaListenerContainerFactory with batch set tot true
     */
    private ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> getNewRetryContainerFactory(long idleMillis) {
        ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(newRetryConsumerFactory());
        factory.getContainerProperties().setIdleBetweenPolls(idleMillis);
        factory.setBatchListener(true);
        return factory;
    }

    /**
     * Creates a concurrent kafka listener container factory.
     * For each message a kafka listener receives this factory will create a container to process the message.
     * Filter strategy is filtering out messages with timestamps after the app started
     *
     * @return A ConcurrentKafkaListenerContainerFactory
     */
    private ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> getNewErrorContainerFactory() {
        var currentTime = Instant.now().toEpochMilli();
        ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(newMainConsumerFactory());
        factory.setRecordFilterStrategy(consumerRecord -> currentTime < consumerRecord.timestamp());
        return factory;
    }

    /**
     *
     * @return ConcurrentKafkaListenerContainerFactory with default configuration
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend>
    kafkaListenerContainerFactory() {
        return getNewDefaultContainerFactory();
    }

    /**
     *
     * @return ConcurrentKafkaListenerContainerFactory with retry configuration
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend>
    kafkaRetryListenerContainerFactory() {

        var idleMillis = retryThrottleSeconds * 1000L;

        return getNewRetryContainerFactory(idleMillis);
    }

    /**
     *
     * @return ConcurrentKafkaListenerContainerFactory with error configuration
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend>
    kafkaErrorListenerContainerFactory() {
        return getNewErrorContainerFactory();
    }

}
