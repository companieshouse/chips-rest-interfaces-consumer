package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.service.ServiceException;

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

    @InjectMocks
    private MessageProducerImpl messageProducer;

    @Test
    void writeToTopic() throws ServiceException {
        var data = new ChipsRestInterfacesSend();
        data.setData("TEST");
        var topic = "TOPIC";

        when(kafkaTemplate.send(topic, data)).thenReturn(future);
        messageProducer.writeToTopic(data, topic);

        verify(kafkaTemplate, times(1)).send(topic, data);
    }
}