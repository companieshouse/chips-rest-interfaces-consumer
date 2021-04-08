package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.serializer.DeserializationException;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageRejectionServiceImplTest {

    private static final String TOPIC = "test-topic";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private SlackMessagingService slackMessagingService;

    @InjectMocks
    private MessageRejectionServiceImpl messageRejectionService;


    @Test
    void testDeserializeExceptionForMainConsumer() {
        Exception exception = new Exception(new DeserializationException("", new byte[1], false, new SerializationException()));
        ConsumerRecord<?, ?> datum = new ConsumerRecord<>(TOPIC,1,1L, "", "");
        messageRejectionService.handleRejectedMessage(exception, datum);
        String deserializationErrorMessage = "Message rejected - Failed to deserialize message - " + buildSingleMessage(TOPIC,1,1L);
        verify(slackMessagingService, times(1)).sendRejectedErrorMessage(deserializationErrorMessage);
    }

    @Test
    void testNonDeserializeExceptionForMainConsumer() {
        Exception exception = new Exception();
        ConsumerRecord<?, ?> datum = new ConsumerRecord<>(TOPIC,1,1L, "", "");
        messageRejectionService.handleRejectedMessage(exception, datum);
        String deserializationErrorMessage = "Failed to deserialize message - " + buildSingleMessage(TOPIC,1,1L);
        verify(slackMessagingService, never()).sendRejectedErrorMessage(deserializationErrorMessage);
    }

    private String buildSingleMessage(String topic, int partition, long offset) {
        return "topic: " + topic + ", partition: " + partition + ", offset: " + offset;
    }
}
