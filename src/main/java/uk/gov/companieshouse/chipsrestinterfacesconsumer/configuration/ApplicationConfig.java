package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean("incoming-message-consumer")
    MessageConsumer incomingMessageConsumer(ApplicationLogger logger,
                                                   MessageProcessorService messageProcessorService,
                                                   DeserializerFactory deserializerFactory,
                                                   @Qualifier("incoming-consumer-group") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer,
                "incoming-message-consumer");
    }

    @Bean("retry-message-consumer")
    MessageConsumer retryMessageConsumer(ApplicationLogger logger,
                                                MessageProcessorService messageProcessorService,
                                                DeserializerFactory deserializerFactory,
                                                @Qualifier("retry-consumer-group") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer,
                "retry-message-consumer");
    }
}
