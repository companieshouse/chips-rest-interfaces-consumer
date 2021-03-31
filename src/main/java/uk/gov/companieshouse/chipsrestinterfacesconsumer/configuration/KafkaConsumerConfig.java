package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.avro.AvroDeserializer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private static final int MINIMUM_POLL_INTERVAL = 300000;

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleSeconds;

    @Autowired
    private Supplier<Long> timestampNow;

    @Autowired
    private SlackMessagingService slackMessagingService;

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
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                AvroDeserializer.class);
        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest");
        props.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false);
        return props;
    }

    /**
     *
     * @return A consumer factory with no delay between polls
     */
    private ConsumerFactory<String, ChipsRestInterfacesSend> newMainConsumerFactory() {
        var props = getDefaultConfig();
        var errorHadnlingDeserializer = new ErrorHandlingDeserializer<>(new AvroDeserializer<>(ChipsRestInterfacesSend.class));
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), errorHadnlingDeserializer);
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
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setErrorHandler((exception, data) -> {
            slackMessagingService.sendDeserializationErrorMassage(data.topic(), data.partition(),  data.offset());
        });
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
        factory.setBatchListener(true);

        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setIdleBetweenPolls(idleMillis);
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
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
        var appStartedTime = timestampNow.get();
        ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(newMainConsumerFactory());
        factory.setRecordFilterStrategy(consumerRecord -> appStartedTime < consumerRecord.timestamp());
        factory.setAckDiscarded(false);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
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
