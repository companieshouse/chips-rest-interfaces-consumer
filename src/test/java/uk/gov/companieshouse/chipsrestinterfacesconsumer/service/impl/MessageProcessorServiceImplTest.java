package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.MessageProducer;
import uk.gov.companieshouse.service.ServiceException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorServiceImplTest {

    @Mock
    private ChipsRestClient chipsRestClient;

    @Mock
    private MessageProducer retryMessageProducer;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MessageProcessorServiceImpl messageProcessorService;

    @Test
    void processMessageTest() throws ServiceException {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();

        messageProcessorService.processMessage(chipsKafkaMessage);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsKafkaMessage));
    }

    @Test
    void testRetryIsCalled() throws ServiceException {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();
        doThrow(RestClientException.class).when(chipsRestClient).sendToChips(chipsKafkaMessage);
        messageProcessorService.processMessage(chipsKafkaMessage);
        verify(retryMessageProducer, times(1)).writeToQueue(eq(chipsKafkaMessage));
    }
}