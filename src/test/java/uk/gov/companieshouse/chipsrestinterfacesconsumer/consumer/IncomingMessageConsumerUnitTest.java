package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.consumer.CHConsumer;
import uk.gov.companieshouse.kafka.message.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomingMessageConsumerUnitTest {

    private final static String ERROR_MESSAGE = "Failed to read message from queue";
    private final static String LOG_MESSAGE_KEY = "message";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private CHConsumer consumer;

    @InjectMocks
    private IncomingMessageConsumer incomingMessageConsumer;

    @Captor
    private ArgumentCaptor<Map<String, Object>> loggingDataMapCaptor;

    @Test
    void initialisationTest() {
        incomingMessageConsumer.init();
        verify(consumer).connect();
    }

    @Test
    void destroyTest() {
        incomingMessageConsumer.close();
        verify(consumer).close();
    }

    @Test
    void testReadNoMessage() {
        List<Message> messages = Collections.emptyList();
        when(consumer.consume()).thenReturn(messages);

        Collection<ChipsKafkaMessage> result = incomingMessageConsumer.read();

        assertTrue(result.isEmpty());
    }

    @Test
    void testReadValidMessage() {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        // TODO have a real message or something resembling it
        message.setValue("{}".getBytes());
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);

        Collection<ChipsKafkaMessage> result = incomingMessageConsumer.read();

        assertEquals(1, result.size());
        assertNotNull(result.iterator().next());
    }

    @Test
    void testDeserializeExceptionIsCaught() {
        String messageValue = "bad message";
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setValue(messageValue.getBytes());
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.read();

        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(
                eq(ERROR_MESSAGE),
                any(Exception.class),
                loggingDataMapCaptor.capture());
        Map<String, Object> loggingDataMap = loggingDataMapCaptor.getValue();
        assertEquals(messageValue, loggingDataMap.get(LOG_MESSAGE_KEY));
    }

    @Test
    void testDeserializeExceptionIsCaughtWithNullMessageValue() {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setValue(null);
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.read();
        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(
                eq(ERROR_MESSAGE),
                any(Exception.class),
                loggingDataMapCaptor.capture());
        Map<String, Object> loggingDataMap = loggingDataMapCaptor.getValue();
        assertEquals("", loggingDataMap.get(LOG_MESSAGE_KEY));
    }
}
