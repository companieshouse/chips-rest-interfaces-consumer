package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlackMessagingServiceImplTest {

    private static final String SLACK_CHANNEL = "Test Channel";
    public static final String SLACK_ERROR_MESSAGE = "In %s mode, this is a test for message %s";

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private SlackMessagingServiceImpl slackMessagingServiceImpl;

    @Test
    void test() {
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"slackChannel", SLACK_CHANNEL);
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"inErrorMode", false);
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"slackErrorMessage", SLACK_ERROR_MESSAGE);
        //slackMessagingServiceImpl.sendMessage();
        //verify(logger).infoContext(KAFKA_MESSAGE_ID, String.format("Message sent to: %s", SLACK_CHANNEL));
    }
}
