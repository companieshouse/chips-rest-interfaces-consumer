package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    @Mock
    private ApplicationLogger logger;

    @Mock
    private AvroSerializer avroSerializer;

    @Mock
    private CHKafkaProducer producer;

    @InjectMocks
    private MessageProducerImpl messageProducerImpl;

    @Test
    void testSuccessfulWriteToTopic()
            throws ServiceException, IOException, ExecutionException, InterruptedException {
        Future<RecordMetadata> mockedFuture = Mockito.mock(Future.class);

        when(producer.sendAndReturnFuture(any())).thenReturn(mockedFuture);
        messageProducerImpl.writeToTopic(getDummyChipsKafkaMessage());

        verify(mockedFuture, times(1)).get();
    }

    @Test
    void testServiceExceptionIsThrownWhenSerializerThrowsIOExcpetion()
            throws IOException {
        doThrow(IOException.class).when(avroSerializer).serialize(any(), any());
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(getDummyChipsKafkaMessage()));
    }

    @Test
    void testServiceExceptionIsThrownWhenFutureThrowsInterruptedException()
            throws ExecutionException, InterruptedException {
        Future<RecordMetadata> faultyMockedFuture = Mockito.mock(Future.class);
        when(producer.sendAndReturnFuture(any())).thenReturn(faultyMockedFuture);
        doThrow(InterruptedException.class).when(faultyMockedFuture).get();
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(getDummyChipsKafkaMessage()));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testServiceExceptionIsThrownWhenFutureThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        Future<RecordMetadata> faultyMockedFuture = Mockito.mock(Future.class);
        when(producer.sendAndReturnFuture(any())).thenReturn(faultyMockedFuture );
        doThrow(ExecutionException.class).when(faultyMockedFuture).get();
        assertThrows(ServiceException.class, () -> messageProducerImpl.writeToTopic(getDummyChipsKafkaMessage()));
    }

    private ChipsKafkaMessage getDummyChipsKafkaMessage() {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();
        chipsKafkaMessage.setAppId(APP_ID);
        chipsKafkaMessage.setAttempt(ATTEMPT);
        chipsKafkaMessage.setMessageId(MESSAGE_ID);
        chipsKafkaMessage.setData(DATA);
        chipsKafkaMessage.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        chipsKafkaMessage.setCreatedAt(CREATED_AT);
        return chipsKafkaMessage;
    }
}
