package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OutgoingMessageProducerUnitTest {

    private static final Future<RecordMetadata> MOCKED_FUTURE = Mockito.mock(Future.class);
    private static final Future<RecordMetadata> FAULTY_MOCKED_FUTURE = Mockito.mock(Future.class);

    private static final String APP_ID = "chips-rest-interfaces-consumer";
    private static int ATTEMPT = 4;
    private static final String MESSAGE_ID = "abc";
    private static final String DATA = "{subject: testing}";
    private static final String CHIPS_REST_ENDPOINT = "http://nowhere:1234";
    private static final String CREATED_AT = "01 Jan 2021 08:00:00";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private AvroSerializer avroSerializer;

    @Mock
    private AvroSerializer faultyAvroSerializer;

    @Mock
    private CHKafkaProducer producer;


    @Test
    public void testSuccessfulWriteToQueue()
            throws ServiceException, IOException, ExecutionException, InterruptedException {
        OutgoingMessageProducer  outgoingMessageProducer =
                new OutgoingMessageProducer(logger, avroSerializer, producer);
        String avroDataString =
                "{\"app_id\": \"chips-rest-interfaces-consumer\", \"attempt\": 4, \"message_id\": \"abc\", \"data\": \"{subject: testing}\", \"chips_rest_endpoint\": \"http://nowhere:1234\", \"created_at\": \"01 Jan 2021 08:00:00\"}";
        byte[] avroByteArray = avroDataString.getBytes();
        when(avroSerializer.serialize(any(), any())).thenReturn(avroByteArray);
        when(producer.sendAndReturnFuture(any())).thenReturn(MOCKED_FUTURE);
        outgoingMessageProducer.writeToQueue(getDummyChipsKafkaMessage());
        verify(MOCKED_FUTURE, times(1)).get();
    }


    @Test
    void testServiceExcpetionIsThrownWhenSerializerThrowsIOExcpetion()
            throws IOException {
        OutgoingMessageProducer outgoingMessageProducer =
                new OutgoingMessageProducer(logger, faultyAvroSerializer, producer);
        doThrow(IOException.class).when(faultyAvroSerializer).serialize(any(), any());
        assertThrows(ServiceException.class, () -> {
            outgoingMessageProducer.writeToQueue(getDummyChipsKafkaMessage());
        });
    }

    @Test
    void testServiceExcpetionIsThrownWhenFutureThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        OutgoingMessageProducer outgoingMessageProducer =
                new OutgoingMessageProducer(logger, avroSerializer, producer);
        when(producer.sendAndReturnFuture(any())).thenReturn(FAULTY_MOCKED_FUTURE);
        doThrow(ExecutionException.class).when(FAULTY_MOCKED_FUTURE).get();
        assertThrows(ServiceException.class, () -> {
            outgoingMessageProducer.writeToQueue(getDummyChipsKafkaMessage());
        });
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
