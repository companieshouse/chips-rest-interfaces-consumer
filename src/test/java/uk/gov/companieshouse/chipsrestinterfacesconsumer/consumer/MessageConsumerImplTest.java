package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class MessageConsumerImplTest {

    private final static String LOG_MESSAGE_KEY = "message";
    private final static String LOG_OFFSET_KEY = "Message Offset";
    private final static String LOG_MESSAGE_ID_KEY = "Deserialised message id";
    private final static String MESSAGE_CONSUMER_ID = "consumer id";
    private final static String ERROR_MESSAGE = MESSAGE_CONSUMER_ID + " - Failed to read message from queue";

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

    private MessageConsumerImpl messageConsumer;

    @Captor
    private ArgumentCaptor<Map<String, Object>> loggingDataMapCaptor;

    @BeforeEach
    void setup() {
        messageConsumer = new MessageConsumerImpl(
                logger,
                messageProcessorService,
                deserializerFactory,
                consumer,
                MESSAGE_CONSUMER_ID
        );
    }

    @Test
    void initialisationTest() {
        messageConsumer.init();
        verify(consumer).connect();
    }

    @Test
    void destroyTest() {
        messageConsumer.close();
        verify(consumer).close();
    }

    @Test
    void testReadNoMessage() throws ServiceException {
        List<Message> messages = Collections.emptyList();
        when(consumer.consume()).thenReturn(messages);

        messageConsumer.readAndProcess();

        verify(messageProcessorService, times(0)).processMessage(any());
    }

    @Test
    void testReadValidMessage() throws ServiceException, DeserializationException {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        Long offset = 123L;
        message.setOffset(offset);
        messages.add(message);

        ChipsRestInterfacesSend deserializedMessage = new ChipsRestInterfacesSend();
        String messageId = "H747J848J33DSF";
        deserializedMessage.setMessageId(messageId);

        when(consumer.consume()).thenReturn(messages);
        when(deserializerFactory.getSpecificRecordDeserializer(ChipsRestInterfacesSend.class)).thenReturn(avroDeserializer);
        when(avroDeserializer.fromBinary(message, ChipsRestInterfacesSend.getClassSchema())).thenReturn(deserializedMessage);

        messageConsumer.readAndProcess();

        verify(messageProcessorService, times(messages.size())).processMessage(deserializedMessage);
        verify(consumer, times(messages.size())).commit(message);

        verify(logger, times(1)).info(
                String.format("%s - Message offset %s retrieved, processing", MESSAGE_CONSUMER_ID, offset));
        verify(logger, times(1)).info(
                eq(String.format("%s - Message deserialised successfully", MESSAGE_CONSUMER_ID)),
                loggingDataMapCaptor.capture());
        Map<String, Object> loggingDataMap = loggingDataMapCaptor.getValue();
        assertEquals(offset, loggingDataMap.get(LOG_OFFSET_KEY));
        assertEquals(messageId, loggingDataMap.get(LOG_MESSAGE_ID_KEY));
        verify(logger, times(1)).info(
                String.format("%s - Message offset %s processed, committing offset", MESSAGE_CONSUMER_ID, offset));
    }

    @Test
    void testDeserializeExceptionIsCaught() throws DeserializationException {
        String messageValue = "bad message";
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setValue(messageValue.getBytes());
        messages.add(message);

        when(consumer.consume()).thenReturn(messages);
        Exception e = new Exception();
        DeserializationException deserializationException = new DeserializationException("error", e);
        when(deserializerFactory.getSpecificRecordDeserializer(ChipsRestInterfacesSend.class)).thenReturn(avroDeserializer);
        when(avroDeserializer.fromBinary(message, ChipsRestInterfacesSend.getClassSchema())).thenThrow(deserializationException);

        messageConsumer.readAndProcess();

        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(
                eq(ERROR_MESSAGE),
                eq(deserializationException),
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

        messageConsumer.readAndProcess();

        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(
                eq(ERROR_MESSAGE),
                any(NullPointerException.class),
                loggingDataMapCaptor.capture());
        Map<String, Object> loggingDataMap = loggingDataMapCaptor.getValue();
        assertEquals("", loggingDataMap.get(LOG_MESSAGE_KEY));
    }
}
