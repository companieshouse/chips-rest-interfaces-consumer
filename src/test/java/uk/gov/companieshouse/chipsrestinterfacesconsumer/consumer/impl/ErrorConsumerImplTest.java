package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.MessageHeaders;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ErrorConsumerImplTest {

    private static final String DATA = "DATA";
    private static final String ERROR_CONSUMER_ID = "error-consumer";

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private ErrorConsumerImpl errorConsumer;

    private ChipsRestInterfacesSend data;
    private MessageHeaders headers;

    @BeforeEach
    void init() {
        data = new ChipsRestInterfacesSend();
        data.setData(DATA);
        headers = new MessageHeaders(Collections.singletonMap("Key", "Value"));
    }

    @Test
    void readAndProcessErrorTopic() throws ServiceException {
        errorConsumer.readAndProcessErrorTopic(data, headers);

        verify(messageProcessorService, times(1)).processMessage(ERROR_CONSUMER_ID, data);
    }
}