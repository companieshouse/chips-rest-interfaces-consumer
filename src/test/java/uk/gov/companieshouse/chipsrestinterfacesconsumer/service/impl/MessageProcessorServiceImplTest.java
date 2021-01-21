package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.MessageProducer;
import uk.gov.companieshouse.service.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
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
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(retryMessageProducer, times(0)).writeToTopic(eq(chipsRestInterfacesSend));
    }

    @Test
    void testRetryIsCalled() throws ServiceException {
        ReflectionTestUtils.setField(messageProcessorService, "maxRetryAttempts", 10);
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setAttempt(0);
        RestClientException restClientException = new RestClientException("restClientException");
        doThrow(restClientException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq("Error sending message to chips, will place on retry queue"), eq(restClientException), anyMap());
        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(retryMessageProducer, times(1)).writeToTopic(eq(chipsRestInterfacesSend));
    }

    @Test
    void testMaxAttemptsReached() throws ServiceException {
        ReflectionTestUtils.setField(messageProcessorService, "maxRetryAttempts", 10);
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setAttempt(10);
        RestClientException restClientException = new RestClientException("restClientException");
        doThrow(restClientException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(retryMessageProducer, times(0)).writeToTopic(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq("Error max attempts reached"), eq(restClientException), anyMap());
    }
}