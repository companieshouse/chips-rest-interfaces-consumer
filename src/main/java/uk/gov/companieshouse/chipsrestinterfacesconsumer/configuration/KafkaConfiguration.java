package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.resilience.CHConsumerType;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.Collections;

@Configuration
class KafkaConfiguration {

    public static final String APPLICATION_NAME = "cric";

    @Value("${kafka.main.consumer.group.name}")
    private String mainConsumerGroupName;

    @Value("${kafka.retry.consumer.group.name}")
    private String retryConsumerGroupName;

    @Value("${kafka.error.consumer.group.name}")
    private String errorConsumerGroupName;

    @Value("${kafka.broker.address}")
    private String brokerAddress;

    @Value("${kafka.consumer.topic}")
    private String mainTopicName;

    @Value("${kafka.retry.topic}")
    private String retryTopicName;

    @Value("${kafka.error.topic}")
    private String errorTopicName;

    @Value("${kafka.consumer.poll.timeout.ms:100}")
    private long pollTimeout;

    @Value("${kafka.consumer.max.poll.interval.ms:300000}")
    private int maxPollIntervalMs;

    @Value("${kafka.producer.retries}")
    private int retries;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleSeconds;

    @Bean
    DeserializerFactory getDeserializerFactory() {
        return new DeserializerFactory();
    }

    @Bean
    SerializerFactory getSerializerFactory() {
        return new SerializerFactory();
    }

    @Bean("main-consumer-group")
    @Lazy
    CHKafkaResilientConsumerGroup getMainConsumer() {
        return new CHKafkaResilientConsumerGroup(getMainConsumerConfig(), CHConsumerType.MAIN_CONSUMER);
    }

    @Bean
    ConsumerConfig getMainConsumerConfig() {
        ConsumerConfig config = new ConsumerConfig(APPLICATION_NAME);
        config.setBrokerAddresses(new String[]{brokerAddress});
        config.setTopics(Collections.singletonList(mainTopicName));
        config.setPollTimeout(pollTimeout);
        config.setMaxPollIntervalMilliSeconds(maxPollIntervalMs);
        config.setGroupName(mainConsumerGroupName);
        return config;
    }

    @Bean("retry-consumer-group")
    @Lazy
    CHKafkaResilientConsumerGroup getRetryConsumer() {
        return new CHKafkaResilientConsumerGroup(getRetryConsumerConfig(), CHConsumerType.RETRY_CONSUMER);
    }

    ConsumerConfig getRetryConsumerConfig() {
        ConsumerConfig config = new ConsumerConfig(APPLICATION_NAME);
        config.setBrokerAddresses(new String[]{brokerAddress});
        config.setTopics(Collections.singletonList(mainTopicName));
        config.setPollTimeout(pollTimeout);
        config.setMaxPollIntervalMilliSeconds((Math.toIntExact(retryThrottleSeconds) * 1000) + maxPollIntervalMs);
        config.setGroupName(retryConsumerGroupName);
        return config;
    }

    @Bean("error-consumer-group")
    @Lazy
    CHKafkaResilientConsumerGroup getErrorConsumer() {
        return new CHKafkaResilientConsumerGroup(getErrorConsumerConfig(), CHConsumerType.ERROR_CONSUMER);
    }

    ConsumerConfig getErrorConsumerConfig() {
        ConsumerConfig config = new ConsumerConfig(APPLICATION_NAME);
        config.setBrokerAddresses(new String[]{brokerAddress});
        config.setTopics(Collections.singletonList(mainTopicName));
        config.setPollTimeout(pollTimeout);
        config.setMaxPollIntervalMilliSeconds(maxPollIntervalMs);
        config.setGroupName(errorConsumerGroupName);
        return config;
    }

    @Bean
    CHKafkaProducer getMessageProducer() {
        return new CHKafkaProducer(getMessageProducerConfig());
    }

    ProducerConfig getMessageProducerConfig() {
        ProducerConfig config = new ProducerConfig();
        config.setBrokerAddresses(new String[]{brokerAddress});
        config.setRoundRobinPartitioner(true);
        config.setAcks(Acks.WAIT_FOR_ALL);
        config.setRetries(retries);
        return config;
    }
}
