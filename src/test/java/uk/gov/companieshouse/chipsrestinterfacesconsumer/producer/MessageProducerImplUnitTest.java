package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.service.ServiceException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageProducerImplUnitTest {

    private static final String APP_ID = "chips-rest-interfaces-consumer";
    private static final int ATTEMPT = 4;
    private static final String MESSAGE_ID = "abc";
    private static final String DATA = "{subject: testing}";
    private static final String CHIPS_REST_ENDPOINT = "http://nowhere:1234";
    private static final String CREATED_AT = "1610543925";
    private static final String TEST_TOPIC = "test-topic";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private SerializerFactory serializerFactory;

    @Mock
    private AvroSerializer<ChipsRestInterfacesSend> avroSerializer;

    @Mock
    private CHKafkaProducer producer;

    @Mock
    private Future<RecordMetadata> mockedFuture;

    @InjectMocks
    private MessageProducerImpl messageProducerImpl;

    @Captor
    private ArgumentCaptor<Message> kafkaMessageCaptor;

    @BeforeEach
    void setup() {
        when(serializerFactory.getSpecificRecordSerializer(ChipsRestInterfacesSend.class)).thenReturn(avroSerializer);
    }

    @Test
    void testSuccessfulWriteToTopic()
            throws ServiceException, ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(mockedFuture);
        messageProducerImpl.writeToTopic(getDummyChipsRestInterfacesSend(), TEST_TOPIC);
        verify(mockedFuture, times(1)).get();
        verify(producer, times(1)).sendAndReturnFuture(kafkaMessageCaptor.capture());
        Message kafkaMessage = kafkaMessageCaptor.getValue();
        assertEquals(TEST_TOPIC, kafkaMessage.getTopic());
    }

    @Test
    void testServiceExceptionIsThrownWhenSerializerThrowsSerializationException()
            throws SerializationException {
        SerializationException serializationException = new SerializationException("error", new Exception());
        ChipsRestInterfacesSend chipsRestInterfacesSend = getDummyChipsRestInterfacesSend();
        when(avroSerializer.toBinary(chipsRestInterfacesSend)).thenThrow(serializationException);
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(chipsRestInterfacesSend, TEST_TOPIC));
        verify(producer, times(0)).sendAndReturnFuture(any());
    }

    @Test
    void testServiceExceptionIsThrownWhenFutureThrowsInterruptedException()
            throws ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(mockedFuture);
        doThrow(InterruptedException.class).when(mockedFuture).get();
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(getDummyChipsRestInterfacesSend(), TEST_TOPIC));
        assertTrue(Thread.currentThread().isInterrupted());
        verify(producer, times(1)).sendAndReturnFuture(kafkaMessageCaptor.capture());
        Message kafkaMessage = kafkaMessageCaptor.getValue();
        assertEquals(TEST_TOPIC, kafkaMessage.getTopic());
    }

    @Test
    void testServiceExceptionIsThrownWhenFutureThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(mockedFuture);
        doThrow(ExecutionException.class).when(mockedFuture).get();
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(getDummyChipsRestInterfacesSend(), TEST_TOPIC));
        verify(producer, times(1)).sendAndReturnFuture(kafkaMessageCaptor.capture());
        Message kafkaMessage = kafkaMessageCaptor.getValue();
        assertEquals(TEST_TOPIC, kafkaMessage.getTopic());
    }

    private ChipsRestInterfacesSend getDummyChipsRestInterfacesSend() {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setAppId(APP_ID);
        chipsRestInterfacesSend.setAttempt(ATTEMPT);
        chipsRestInterfacesSend.setMessageId(MESSAGE_ID);
        chipsRestInterfacesSend.setData(DATA);
        chipsRestInterfacesSend.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        chipsRestInterfacesSend.setCreatedAt(CREATED_AT);
        return chipsRestInterfacesSend;
    }
}
