package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    private static final String MAIN_CONSUMER_ID = "main-message-consumer";
    private static final String RETRY_CONSUMER_ID = "retry-message-consumer";
    private static final String ERROR_CONSUMER_ID = "error-message-consumer";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private DeserializerFactory deserializerFactory;

    @Mock
    private CHKafkaResilientConsumerGroup consumer;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setup() {
        applicationConfig = new ApplicationConfig();
    }

    @Test
    void testRestTemplate() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        RestTemplate result = applicationConfig.restTemplate(restTemplateBuilder);

        verify(restTemplateBuilder, times(1)).build();
        assertEquals(restTemplate, result);
    }

    @Test
    void testMainMessageConsumer() {
        MessageConsumer messageConsumer = applicationConfig.mainMessageConsumer(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer);

        assertEquals(MAIN_CONSUMER_ID, ((MessageConsumerImpl) messageConsumer).getId());
    }

    @Test
    void testRetryMessageConsumer() {
        MessageConsumer messageConsumer = applicationConfig.retryMessageConsumer(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer);

        assertEquals(RETRY_CONSUMER_ID, ((MessageConsumerImpl) messageConsumer).getId());
    }

    @Test
    void testErrorMessageConsumer() {
        MessageConsumer messageConsumer = applicationConfig.errorMessageConsumer(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer);

        assertEquals(ERROR_CONSUMER_ID, ((MessageConsumerImpl) messageConsumer).getId());
    }
}
