package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

import java.util.Collections;

@Configuration
class KafkaConfiguration {

    @Value("${kafka.group.name}")
    private String groupName;

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.consumer.topic}")
    private String topicName;

    @Value("${kafka.consumer.pollTimeout:100}")
    private long pollTimeout;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Bean
    DeserializerFactory getDeserializerFactory() {
        return new DeserializerFactory();
    }

    @Bean
    CHKafkaConsumerGroup getIncomingConsumer() {
        return new CHKafkaConsumerGroup(getIncomingConsumerConfig());
    }

    ConsumerConfig getIncomingConsumerConfig() {
        ConsumerConfig config = new ConsumerConfig();
        config.setBrokerAddresses(new String[] { brokerAddress });
        config.setTopics(Collections.singletonList(topicName));
        config.setPollTimeout(pollTimeout);
        config.setGroupName(groupName);
        return config;
    }

    @Bean
    CHKafkaProducer getRetryMessageProducer() {
        ProducerConfig config = new ProducerConfig();
        ProducerConfigHelper.assignBrokerAddresses(config);
        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(retries);
        return new CHKafkaProducer(config);
    }
}
