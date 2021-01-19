package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.KafkaRestClient;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public KafkaRestClient restClient(RestTemplate restTemplate) {
        return new KafkaRestClient(restTemplate);
    }

    @Bean("incoming-message-consumer")
    public MessageConsumer incomingMessageConsumer(@Qualifier("incoming-consumer-group") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(consumer, "incoming-message-consumer");
    }

    @Bean("retry-message-consumer")
    public MessageConsumer retryMessageConsumer(@Qualifier("retry-consumer-group") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(consumer, "retry-message-consumer");
    }
}
