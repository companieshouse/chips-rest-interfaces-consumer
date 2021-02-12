package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MainConsumerImplTest {

    private static final String DATA = "DATA";
    private static final String MAIN_CONSUMER_ID = "main-consumer";
    private static final String RETRY_CONSUMER_ID = "retry-consumer";

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MainConsumerImpl mainConsumer;

    private ChipsRestInterfacesSend data;
    private MessageHeaders headers;

    @BeforeEach
    void init() {
        data = new ChipsRestInterfacesSend();
        data.setData(DATA);
        headers = new MessageHeaders(Collections.singletonMap("Key", "Value"));
    }

    @Test
    void readAndProcessMainTopic() throws ServiceException {
        mainConsumer.readAndProcessMainTopic(data, headers);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data);
    }

    @Test
    void readAndProcessRetryTopic() throws ServiceException {
        mainConsumer.readAndProcessRetryTopic(data, headers);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data);
    }
}