package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

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
    private static final String MESSAGE_ID = "abc-123";
    private static final String SECOND_MESSAGE_ID = "cde-345";

    @Mock
    private Acknowledgment acknowledgment;

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
        data.setMessageId(MESSAGE_ID);
        data.setData(DATA);
        secondData = new ChipsRestInterfacesSend();
        secondData.setMessageId(SECOND_MESSAGE_ID);
        secondData.setData(SECOND_DATA);
    }

    @Test
    void readAndProcessMainTopic() {
        mainConsumer.readAndProcessMainTopic(data, acknowledgment, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data);
        assertEquals(0, data.getAttempt());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void readAndProcessMainTopicWithAttempts() {
        data.setAttempt(5);
        mainConsumer.readAndProcessMainTopic(data, acknowledgment, 0L, 0, MAIN_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(MAIN_CONSUMER_ID, data);
        assertEquals(0, data.getAttempt());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void readAndProcessRetryTopicWithEmptyFailedMessageIds() {
        List<ChipsRestInterfacesSend> messageList = Arrays.asList(data, secondData);
        List<Long> offsets = Arrays.asList(0L, 1L);
        List<Integer> partitions = Arrays.asList(0, 0);

        List<String> failedMessageIds = new ArrayList<>();

        mainConsumer.readAndProcessRetryTopic(messageList, acknowledgment, offsets, partitions, RETRY_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data);
        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, secondData);
        verify(slackMessagingService, never()).sendMessage(failedMessageIds);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void readAndProcessRetryTopicWithSlackFlagFalse() {
        List<ChipsRestInterfacesSend> messageList = Arrays.asList(data, secondData);
        List<Long> offsets = Arrays.asList(0L, 1L);
        List<Integer> partitions = Arrays.asList(0, 0);

        ReflectionTestUtils.setField(mainConsumer, "doSendSlackMessages", false);
        List<String> failedMessageIds = new ArrayList<>();
        failedMessageIds.add(MESSAGE_ID);
        failedMessageIds.add(SECOND_MESSAGE_ID);
        mainConsumer.readAndProcessRetryTopic(messageList, acknowledgment, offsets, partitions, RETRY_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data);
        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, secondData);

        verify(slackMessagingService, never()).sendMessage(failedMessageIds);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void readAndProcessRetryTopicWithSlackFlagTrue() {
        List<ChipsRestInterfacesSend> messageList = Arrays.asList(data, secondData);
        List<Long> offsets = Arrays.asList(0L, 1L);
        List<Integer> partitions = Arrays.asList(0, 0);

        ReflectionTestUtils.setField(mainConsumer, "doSendSlackMessages", true);
        List<String> failedMessageIds = new ArrayList<>();
        failedMessageIds.add(MESSAGE_ID);
        failedMessageIds.add(SECOND_MESSAGE_ID);
        mainConsumer.readAndProcessRetryTopic(messageList, acknowledgment, offsets, partitions, RETRY_CONSUMER_ID);

        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, data);
        verify(messageProcessorService, times(1)).processMessage(RETRY_CONSUMER_ID, secondData);
        verify(slackMessagingService, times(1)).sendMessage(failedMessageIds);
        verify(acknowledgment, times(1)).acknowledge();
    }
}