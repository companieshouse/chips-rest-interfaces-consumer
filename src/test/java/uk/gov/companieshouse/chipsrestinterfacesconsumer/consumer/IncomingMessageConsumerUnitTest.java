package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.AvroDeserializer;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.service.ServiceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private DeserializerFactory deserializerFactory;

    @Mock
    private AvroDeserializer<ChipsRestInterfacesSend> avroDeserializer;

    @Mock
    private CHKafkaConsumerGroup consumer;

    @Mock
    private MessageProcessorService messageProcessorService;

    @InjectMocks
    private MessageConsumerImpl incomingMessageConsumer;

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
    void testReadNoMessage() throws ServiceException {
        List<Message> messages = Collections.emptyList();
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.readAndProcess();

        verify(messageProcessorService, times(0)).processMessage(any());
    }

    @Test
    void testReadValidMessage() throws ServiceException, DeserializationException {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        ChipsRestInterfacesSend deserializedMessage = new ChipsRestInterfacesSend();
        // TODO have a real message or something resembling it
        message.setValue("{}".getBytes());
        messages.add(message);
        when(deserializerFactory.getSpecificRecordDeserializer(ChipsRestInterfacesSend.class)).thenReturn(avroDeserializer);
        when(avroDeserializer.fromBinary(any(), any())).thenReturn(deserializedMessage);
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.readAndProcess();

        verify(messageProcessorService, times(messages.size())).processMessage(any());
        verify(consumer, times(messages.size())).commit(any());

    }

    @Test
    void testDeserializeExceptionIsCaught() {
        String messageValue = "bad message";
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setValue(messageValue.getBytes());
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.readAndProcess();

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

        incomingMessageConsumer.readAndProcess();
        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(
                eq(ERROR_MESSAGE),
                any(Exception.class),
                loggingDataMapCaptor.capture());
        Map<String, Object> loggingDataMap = loggingDataMapCaptor.getValue();
        assertEquals("", loggingDataMap.get(LOG_MESSAGE_KEY));
    }
}
