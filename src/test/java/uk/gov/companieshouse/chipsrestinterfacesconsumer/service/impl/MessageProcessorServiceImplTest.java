package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.MessageProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorServiceImplTest {
    private static final int MAX_RETRIES = 10;
    private static final String DUMMY_DATA = "{test:data}";
    private static final String LOG_KEY_MESSAGE = "Message";
    private static final String CHIPS_ERROR_MESSAGE = "Error sending message to chips";

    @Mock
    private ChipsRestClient chipsRestClient;

    @Mock
    private MessageProducer retryMessageProducer;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MessageProcessorServiceImpl messageProcessorService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(messageProcessorService, "maxRetryAttempts", MAX_RETRIES);
    }

    @Test
    void processMessageTest() throws ServiceException {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(retryMessageProducer, times(0)).writeToTopic(eq(chipsRestInterfacesSend));
    }

    @Test
    void testRetryIsCalledForGenericException() throws ServiceException {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DUMMY_DATA);
        chipsRestInterfacesSend.setAttempt(0);
        RuntimeException runtimeException = new RuntimeException("runtimeException");
        doThrow(runtimeException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(CHIPS_ERROR_MESSAGE), eq(runtimeException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));

        verify(logger, times(1)).info(eq("Placing message on RETRY topic for attempt 1"), mapArgumentCaptor.capture());
        logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));

        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(retryMessageProducer, times(1)).writeToTopic(eq(chipsRestInterfacesSend));
    }

    @Test
    void testRetryIsCalledForHttpClientErrorException() throws ServiceException {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DUMMY_DATA);
        chipsRestInterfacesSend.setAttempt(0);
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
        doThrow(httpClientErrorException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(CHIPS_ERROR_MESSAGE), eq(httpClientErrorException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));
        assertEquals(HttpStatus.BAD_GATEWAY, logMap.get("HTTP Status Code"));

        verify(logger, times(1)).info(eq("Placing message on RETRY topic for attempt 1"), mapArgumentCaptor.capture());
        logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));
        assertEquals(HttpStatus.BAD_GATEWAY, logMap.get("HTTP Status Code"));

        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(retryMessageProducer, times(1)).writeToTopic(eq(chipsRestInterfacesSend));
    }

    @Test
    void testMaxAttemptsReached() throws ServiceException {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DUMMY_DATA);
        chipsRestInterfacesSend.setAttempt(10);
        RestClientException restClientException = new RestClientException("restClientException");
        doThrow(restClientException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(retryMessageProducer, times(0)).writeToTopic(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(CHIPS_ERROR_MESSAGE), eq(restClientException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));

        verify(logger, times(1))
                .error(eq("Maximum retry attempts " + MAX_RETRIES + " reached, moving message to ERROR topic"), eq(restClientException), mapArgumentCaptor.capture());
        logMap = mapArgumentCaptor.getValue();
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));
    }
}
