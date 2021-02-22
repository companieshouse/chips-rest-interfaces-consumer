package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.avro.AvroSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Value("${kafka.producer.batch.size.bytes}")
    private int batchSizeBytes;

    @Value("${kafka.producer.linger.ms}")
    private int lingerMs;

    @Bean
    public ProducerFactory<String, ChipsRestInterfacesSend> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                brokerAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                AvroSerializer.class);
        configProps.put(
                ProducerConfig.BATCH_SIZE_CONFIG,
                batchSizeBytes);
        configProps.put(
                ProducerConfig.LINGER_MS_CONFIG,
                lingerMs);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, ChipsRestInterfacesSend> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
