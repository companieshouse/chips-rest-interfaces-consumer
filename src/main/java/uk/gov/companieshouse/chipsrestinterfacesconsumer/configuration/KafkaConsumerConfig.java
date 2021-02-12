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
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.avro.AvroDeserializer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleSeconds;

    @Value("${MAX_RETRY_ATTEMPTS}")
    private int maxRetryAttempts;

    @Autowired
    private ApplicationLogger logger;

    @Bean
    public ConsumerFactory<String, ChipsRestInterfacesSend> consumerFactory() {
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
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new AvroDeserializer<>(ChipsRestInterfacesSend.class));
    }

    private ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> getNewDefaultContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend>
    kafkaListenerContainerFactory() {

        return getNewDefaultContainerFactory();
    }

    ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend> getNewRetryContainerFactory(long idleMillis) {
        var factory = getNewDefaultContainerFactory();
        factory.getContainerProperties().setIdleBetweenPolls(idleMillis);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ChipsRestInterfacesSend>
    kafkaRetryListenerContainerFactory() {

        var idleMillis = retryThrottleSeconds * 1000L;

        return getNewRetryContainerFactory(idleMillis);
    }
}
