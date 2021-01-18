package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.KafkaRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

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
    public MessageConsumer incomingMessageConsumer(AutowireCapableBeanFactory beanFactory,
                                                   ApplicationLogger logger,
                                                   @Qualifier("incoming-consumer-group") CHKafkaConsumerGroup consumer,
                                                   MessageProcessorService messageProcessorService,
                                                   DeserializerFactory deserializerFactory) {
        MessageConsumer messageConsumer = new MessageConsumerImpl(
                logger,
                consumer,
                messageProcessorService,
                deserializerFactory);
        beanFactory.initializeBean(messageConsumer, "incoming-message-consumer");
        return messageConsumer;
    }

    @Bean("retry-message-consumer")
    public MessageConsumer retryMessageConsumer(AutowireCapableBeanFactory beanFactory,
                                                ApplicationLogger logger,
                                                @Qualifier("retry-consumer-group") CHKafkaConsumerGroup consumer,
                                                MessageProcessorService messageProcessorService,
                                                DeserializerFactory deserializerFactory) {
        MessageConsumer messageConsumer = new MessageConsumerImpl(
                logger,
                consumer,
                messageProcessorService,
                deserializerFactory);
        beanFactory.initializeBean(messageConsumer, "retry-message-consumer");
        return messageConsumer;
    }
}
