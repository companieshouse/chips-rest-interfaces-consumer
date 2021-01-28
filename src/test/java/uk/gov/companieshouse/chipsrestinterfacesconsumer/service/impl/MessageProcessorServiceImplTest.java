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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.MessageProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorServiceImplTest {
    private static final int MAX_RETRIES = 10;
    private static final String MESSAGE_ID = "H43JG-J765K";
    private static final String DUMMY_DATA = "{test:data}";
    private static final String LOG_KEY_MESSAGE = "Message";
    private static final String LOG_KEY_HTTP_CODE = "HTTP Status Code";
    private static final String CHIPS_ERROR_MESSAGE = "Error sending message id %s to chips";
    private static final String RETRY_TOPIC = "chips-rest-interfaces-send-retry";
    private static final String ERROR_TOPIC = "chips-rest-interfaces-send-error";

    private ChipsRestInterfacesSend chipsRestInterfacesSend;

    @Mock
    private HttpStatusCodeException httpStatusCodeException;

    @Mock
    private ChipsRestClient chipsRestClient;

    @Mock
    private MessageProducer messageProducer;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MessageProcessorServiceImpl messageProcessorService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> mapArgumentCaptor;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(messageProcessorService, "maxRetryAttempts", MAX_RETRIES);
        ReflectionTestUtils.setField(messageProcessorService, "retryTopicName", RETRY_TOPIC);
        ReflectionTestUtils.setField(messageProcessorService, "errorTopicName", ERROR_TOPIC);

        chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DUMMY_DATA);
        chipsRestInterfacesSend.setMessageId(MESSAGE_ID);
        chipsRestInterfacesSend.setAttempt(0);
    }

    @Test
    void processMessageTest() throws ServiceException {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(messageProducer, times(0)).writeToTopic(any(), eq(RETRY_TOPIC));
        verify(messageProducer, times(0)).writeToTopic(any(), eq(ERROR_TOPIC));
    }

    @Test
    void testRetryIsCalled() throws ServiceException {
        RuntimeException runtimeException = new RuntimeException("runtimeException");
        doThrow(runtimeException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(String.format(CHIPS_ERROR_MESSAGE, MESSAGE_ID)), eq(runtimeException), mapArgumentCaptor.capture());
        verifyLogData(mapArgumentCaptor.getValue());

        verify(logger, times(1)).info(eq("Attempt 0 failed for message id " + MESSAGE_ID), mapArgumentCaptor.capture());
        verifyLogData(mapArgumentCaptor.getValue());

        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(messageProducer, times(1)).writeToTopic(chipsRestInterfacesSend, RETRY_TOPIC);
        verify(messageProducer, times(0)).writeToTopic(any(), eq(ERROR_TOPIC));
    }

    @Test
    void testRetryIsCalledForHttpClientErrorException() throws ServiceException {
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
        doThrow(httpClientErrorException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(String.format(CHIPS_ERROR_MESSAGE, MESSAGE_ID)), eq(httpClientErrorException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        verifyLogData(logMap);
        verifyLogHttpCode(logMap);

        verify(logger, times(1)).info(eq("Attempt 0 failed for message id " + MESSAGE_ID), mapArgumentCaptor.capture());
        logMap = mapArgumentCaptor.getValue();
        verifyLogData(logMap);
        verifyLogHttpCode(logMap);

        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(messageProducer, times(1)).writeToTopic(chipsRestInterfacesSend, RETRY_TOPIC);
        verify(messageProducer, times(0)).writeToTopic(any(), eq(ERROR_TOPIC));
    }

    @Test
    void testRetryIsCalledForHttpServerErrorException() throws ServiceException {
        HttpServerErrorException httpServerErrorException = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
        doThrow(httpServerErrorException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(String.format(CHIPS_ERROR_MESSAGE, MESSAGE_ID)), eq(httpServerErrorException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        verifyLogData(logMap);
        verifyLogHttpCode(logMap);

        verify(logger, times(1)).info(eq("Attempt 0 failed for message id " + MESSAGE_ID), mapArgumentCaptor.capture());
        logMap = mapArgumentCaptor.getValue();
        verifyLogData(logMap);
        verifyLogHttpCode(logMap);

        assertEquals(1, chipsRestInterfacesSend.getAttempt());
        verify(messageProducer, times(1)).writeToTopic(chipsRestInterfacesSend, RETRY_TOPIC);
        verify(messageProducer, times(0)).writeToTopic(any(), eq(ERROR_TOPIC));
    }

    @Test
    void testMaxAttemptsReached() throws ServiceException {
        chipsRestInterfacesSend.setAttempt(10);
        RestClientException restClientException = new RestClientException("restClientException");
        doThrow(restClientException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(chipsRestInterfacesSend);
        verify(messageProducer, times(0)).writeToTopic(chipsRestInterfacesSend, RETRY_TOPIC);
        verify(logger, times(1)).error(eq(String.format(CHIPS_ERROR_MESSAGE, MESSAGE_ID)), eq(restClientException), mapArgumentCaptor.capture());
        verifyLogData(mapArgumentCaptor.getValue());

        verify(logger, times(1)).info(eq("Attempt 10 failed for message id " + MESSAGE_ID), mapArgumentCaptor.capture());
        verifyLogData(mapArgumentCaptor.getValue());

        verify(logger, times(1))
                .error(eq("Maximum retry attempts " + MAX_RETRIES + " reached for message id " + MESSAGE_ID), eq(restClientException), mapArgumentCaptor.capture());
        verifyLogData(mapArgumentCaptor.getValue());

        verify(messageProducer, times(0)).writeToTopic(any(), eq(RETRY_TOPIC));
        verify(messageProducer, times(1)).writeToTopic(chipsRestInterfacesSend, ERROR_TOPIC);
    }

    @Test
    void testNullHttpStatusCode() throws ServiceException {
        doThrow(httpStatusCodeException).when(chipsRestClient).sendToChips(chipsRestInterfacesSend);

        messageProcessorService.processMessage(chipsRestInterfacesSend);

        verify(chipsRestClient, times(1)).sendToChips(eq(chipsRestInterfacesSend));
        verify(logger, times(1)).error(eq(String.format(CHIPS_ERROR_MESSAGE, MESSAGE_ID)), eq(httpStatusCodeException), mapArgumentCaptor.capture());
        Map<String, Object> logMap = mapArgumentCaptor.getValue();
        verifyLogData(logMap);
        assertEquals("null", logMap.get(LOG_KEY_HTTP_CODE));
    }

    private void verifyLogData(Map<String, Object> logMap) {
        assertEquals(DUMMY_DATA, logMap.get(LOG_KEY_MESSAGE));
    }

    private void verifyLogHttpCode(Map<String, Object> logMap) {
        assertEquals(HttpStatus.BAD_GATEWAY.toString(), logMap.get(LOG_KEY_HTTP_CODE));
    }
}
