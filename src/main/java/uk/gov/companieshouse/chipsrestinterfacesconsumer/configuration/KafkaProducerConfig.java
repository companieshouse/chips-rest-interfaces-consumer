package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.producer.retries}")
    private int retries;

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
