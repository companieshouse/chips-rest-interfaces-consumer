package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.impl;

import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlackMessagingServiceImplTest {

    private static final String SLACK_CHANNEL = "Test Channel";

    @Mock
    private ApplicationLogger logger;

    @Spy
    @InjectMocks
    private SlackMessagingServiceImpl slackMessagingServiceImpl;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"slackChannel", SLACK_CHANNEL);
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"inErrorMode", false);
    }

    @Test
    void testSuccessfulSlackMessage() throws IOException, SlackApiException {
        doReturn(buildDummyResponse(true)).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).info(String.format("Message sent to: %s", SLACK_CHANNEL));
    }

    @Test
    void testFailedSlackMessage() throws IOException, SlackApiException {
        ChatPostMessageResponse chatPostMessageResponse = buildDummyResponse(false);
        doReturn(chatPostMessageResponse).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).error(String.format("Error message sent but received response: %s",
                chatPostMessageResponse.getError()));
    }

    @Test
    void testFailedSlackMessageWithIOException() throws IOException, SlackApiException {
        IOException io = new IOException(null, null);
        doThrow(io).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).errorContext("Slack error message not sent", io);
    }

    private List<String> buildDummyFailedMessages() {
        List<String> failedMessageIds = new ArrayList<String>();
        failedMessageIds.add("abc-123");
        failedMessageIds.add("cde-345");
        failedMessageIds.add("efg-567");
        return failedMessageIds;
    }

    private ChatPostMessageResponse buildDummyResponse(boolean ok)  {
        ChatPostMessageResponse response = new ChatPostMessageResponse();
        response.setOk(ok);
        if (!ok) {
            response.setError("This is a test error");
        }
        return response;
    }
}
