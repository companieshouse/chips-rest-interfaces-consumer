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
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumerImpl;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    private static final String INCOMING_CONSUMER_ID = "incoming-message-consumer";
    private static final String RETRY_CONSUMER_ID = "retry-message-consumer";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private DeserializerFactory deserializerFactory;

    @Mock
    private CHKafkaConsumerGroup consumer;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setup () {
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
    void testIncomingMessageConsumer() {
        MessageConsumer messageConsumer = applicationConfig.incomingMessageConsumer(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer);

        assertEquals(INCOMING_CONSUMER_ID, ((MessageConsumerImpl)messageConsumer).getId());
    }

    @Test
    void testRetryMessageConsumer() {
        MessageConsumer messageConsumer = applicationConfig.retryMessageConsumer(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer);

        assertEquals(RETRY_CONSUMER_ID, ((MessageConsumerImpl)messageConsumer).getId());
    }
}
