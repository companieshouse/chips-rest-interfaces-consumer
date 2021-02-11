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
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        // list of host:port pairs used for establishing the initial connections to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                brokerAddress);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);

        return props;
    }

    @Bean
    public ProducerFactory<String, ChipsRestInterfacesSend> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, ChipsRestInterfacesSend> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    SerializerFactory getSerializerFactory() {
        return new SerializerFactory();
    }

    @Bean
    CHKafkaProducer getMessageProducer() {
        return new CHKafkaProducer(getMessageProducerConfig());
    }


    uk.gov.companieshouse.kafka.producer.ProducerConfig getMessageProducerConfig() {
        uk.gov.companieshouse.kafka.producer.ProducerConfig config = new uk.gov.companieshouse.kafka.producer.ProducerConfig();
        config.setBrokerAddresses(new String[]{brokerAddress});
        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(retries);
        return config;
    }
}
