package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

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
}
