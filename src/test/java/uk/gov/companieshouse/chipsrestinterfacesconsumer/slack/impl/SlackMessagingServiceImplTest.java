package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.impl;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackMessagingServiceImplTest {

    private static final String SLACK_CHANNEL = "Test Channel";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private MethodsClient methods;

    @InjectMocks
    private SlackMessagingServiceImpl slackMessagingServiceImpl;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"slackChannel", SLACK_CHANNEL);
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"inErrorMode", false);
        ReflectionTestUtils.setField(slackMessagingServiceImpl,"methods", methods);
    }

    @Test
    void testSuccessfulSlackMessage() throws IOException, SlackApiException {
        when(methods.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(buildDummyResponse(true));
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).info(String.format("Message sent to: %s", SLACK_CHANNEL));
    }

    @Test
    void testFailedSlackMessage() throws IOException, SlackApiException {
        ChatPostMessageResponse chatPostMessageResponse = buildDummyResponse(false);
        when(methods.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).info(String.format("Error message sent but received response: %s",
                chatPostMessageResponse.getError()));
    }

    @Test
    void testFailedSlackMessageWithIOException() throws IOException, SlackApiException {
        IOException io = new IOException(null, null);
        doThrow(io).when(methods).chatPostMessage(any(ChatPostMessageRequest.class));
        slackMessagingServiceImpl.sendMessage(buildDummyFailedMessages());
        verify(logger).errorContext("Slack error message not sent", io);
    }

    private List<String> buildDummyFailedMessages() {
        List<String> failedMessages = new ArrayList<String>();
        failedMessages.add("abc-123");
        failedMessages.add("cde-345");
        failedMessages.add("efg-567");
        return failedMessages;
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
