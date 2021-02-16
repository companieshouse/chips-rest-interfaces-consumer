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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MainConsumerImplTest {

    private static final String DATA = "DATA";
    private static final String SECOND_DATA = "DATA-2";
    private static final String MAIN_CONSUMER_ID = "main-consumer";
    private static final String RETRY_CONSUMER_ID = "retry-consumer";

    @Mock
    private MessageProcessorService messageProcessorService;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private MainConsumerImpl mainConsumer;

    private ChipsRestInterfacesSend data;
    private ChipsRestInterfacesSend secondData;
    private List<ChipsRestInterfacesSend> messageList;

    @BeforeEach
    void init() {
        data = new ChipsRestInterfacesSend();
        data.setData(DATA);
        secondData = new ChipsRestInterfacesSend();
        secondData.setData(SECOND_DATA);
    }

    @Test
    void readAndProcessMainTopic() throws ServiceException {
        mainConsumer.readAndProcessMainTopic(data, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data);
        assertEquals(data.getAttempt(), 0);
    }

    @Test
    void readAndProcessMainTopicWithAttempts() throws ServiceException {
        data.setAttempt(5);
        mainConsumer.readAndProcessMainTopic(data, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data);
        assertEquals(data.getAttempt(), 0);
    }

    @Test
    void readAndProcessRetryTopic() throws ServiceException {
        messageList = Arrays.asList(data, secondData);
        List<Long> offsets = Arrays.asList(0L, 1L);
        List<Integer> partitions = Arrays.asList(0, 0);
        mainConsumer.readAndProcessRetryTopic(messageList, offsets, partitions, RETRY_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data);
        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, secondData);
    }
}