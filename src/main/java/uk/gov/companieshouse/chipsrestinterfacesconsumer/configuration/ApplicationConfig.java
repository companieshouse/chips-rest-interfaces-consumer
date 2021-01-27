package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

@Configuration
class ApplicationConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean("main-message-consumer")
    @Lazy
    MessageConsumer mainMessageConsumer(ApplicationLogger logger,
                                            MessageProcessorService messageProcessorService,
                                            DeserializerFactory deserializerFactory,
                                            @Qualifier("main-consumer-group") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer,
                "main-message-consumer");
    }

    @Bean("retry-message-consumer")
    @Lazy
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

    @Bean("error-message-consumer")
    @Lazy
    MessageConsumer errorMessageConsumer(ApplicationLogger logger,
                                        MessageProcessorService messageProcessorService,
                                        DeserializerFactory deserializerFactory,
                                        @Qualifier("error-message-consumer") CHKafkaConsumerGroup consumer) {
        return new MessageConsumerImpl(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer,
                "error-message-consumer");
    }
}
