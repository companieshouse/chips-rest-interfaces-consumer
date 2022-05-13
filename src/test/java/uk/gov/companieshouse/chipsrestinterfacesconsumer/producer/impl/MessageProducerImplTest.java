package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.impl;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageProducerImplTest {

    @Mock
    private ApplicationLogger logger;

    @Mock
    private KafkaTemplate<String, ChipsRestInterfacesSend> kafkaTemplate;

    @Mock
    private ListenableFuture<SendResult<String, ChipsRestInterfacesSend>> future;

    @Mock
    private SendResult<String, ChipsRestInterfacesSend> sendResult;

    @Captor
    private ArgumentCaptor<ListenableFutureCallback> callbackArgumentCaptor;

    @InjectMocks
    private MessageProducerImpl messageProducer;

    @Test
    void writeToTopic() {
        var messageId = "1234599";
        var data = new ChipsRestInterfacesSend();
        data.setData("TEST");
        data.setMessageId(messageId);
        var topic = "TOPIC";
        var offset = 35L;

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 1), 0, offset, 12345, 1234L, 4, 5);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);

        when(kafkaTemplate.send(topic, data)).thenReturn(future);
        messageProducer.writeToTopic(data, topic);

        verify(kafkaTemplate, times(1)).send(topic, data);

        verify(future, times(1)).addCallback(callbackArgumentCaptor.capture());
        ListenableFutureCallback listenableFutureCallback = callbackArgumentCaptor.getValue();

        listenableFutureCallback.onSuccess(sendResult);

        var expectedMessage = "Sent messageId " + messageId + " to topic " + topic;
        HashMap<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("Partition", 1);
        expectedMap.put("Offset", offset);
        verify(logger, times(1)).infoContext(eq(messageId), eq(expectedMessage), eq(expectedMap));
    }
}