package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlackMessagingServiceImplTest {

    private static final String SLACK_CHANNEL = "Test Channel";
    public static final String KAFKA_MESSAGE_ID = "testMessage";
    public static final String ERROR_MESSAGE = "This is a test error";

    private SlackMessagingService slackMessagingService;

    @Autowired
    ApplicationLogger logger;

    @BeforeEach
    void init() {
        slackMessagingService = new SlackMessagingServiceImpl(logger);
        ReflectionTestUtils.setField(slackMessagingService,"slackChannel", SLACK_CHANNEL);
    }

    @Test
    void test() {
        slackMessagingService.sendMessage(KAFKA_MESSAGE_ID, ERROR_MESSAGE);
        verify(logger).infoContext(KAFKA_MESSAGE_ID, String.format("Message sent to: %s", SLACK_CHANNEL));
    }
}
