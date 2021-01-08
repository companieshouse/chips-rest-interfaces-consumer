package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorServiceImplTest {

    @Mock
    private ChipsRestClient chipsRestClient;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MessageProcessorServiceImpl messageProcessorService;

    @Captor
    private ArgumentCaptor<ChipsKafkaMessage> chipsKafkaMessageArgumentCaptor;

    @Test
    void processMessageTest() {
        ChipsKafkaMessage chipsKafkaMessage1 = new ChipsKafkaMessage();

        messageProcessorService.processMessage(chipsKafkaMessage1);

        verify(chipsRestClient, times(1)).sendToChips(chipsKafkaMessageArgumentCaptor.capture());

        assertEquals(chipsKafkaMessageArgumentCaptor.getValue(), chipsKafkaMessage1);
    }
}