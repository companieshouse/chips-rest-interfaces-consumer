package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
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

    @Mock
    private SlackMessagingService slackMessagingService;

    @InjectMocks
    private MainConsumerImpl mainConsumer;

    private ChipsRestInterfacesSend data;
    private ChipsRestInterfacesSend secondData;

    @BeforeEach
    void init() {
        data = new ChipsRestInterfacesSend();
        data.setData(DATA);
        secondData = new ChipsRestInterfacesSend();
        secondData.setData(SECOND_DATA);
    }

    @Test
    void readAndProcessMainTopic() {
        mainConsumer.readAndProcessMainTopic(data, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data, null);
        assertEquals(0, data.getAttempt());
    }

    @Test
    void readAndProcessMainTopicWithAttempts() {
        data.setAttempt(5);
        mainConsumer.readAndProcessMainTopic(data, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data, null);
        assertEquals(0, data.getAttempt());
    }

    @Test
    void readAndProcessRetryTopic() {
        List<ChipsRestInterfacesSend> messageList = Arrays.asList(data, secondData);
        List<Long> offsets = Arrays.asList(0L, 1L);
        List<Integer> partitions = Arrays.asList(0, 0);

        List<String> failedMessages = new ArrayList<>();

        mainConsumer.readAndProcessRetryTopic(messageList, offsets, partitions, RETRY_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data, failedMessages);
        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, secondData, failedMessages);
        verify(slackMessagingService,  never()).sendMessage(failedMessages);
    }
}