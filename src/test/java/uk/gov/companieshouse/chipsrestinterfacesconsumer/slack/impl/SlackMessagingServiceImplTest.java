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
    }

    @Test
    void testSuccessfulSlackErrorTopicMessage() throws IOException, SlackApiException {
        doReturn(buildDummyResponse(true)).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendErrorTopicMessage(buildDummyFailedMessages());
        verify(logger).info(String.format("Error topic message sent to: %s", SLACK_CHANNEL));
    }

    @Test
    void testFailedSlackErrorTopicMessage() throws IOException, SlackApiException {
        ChatPostMessageResponse chatPostMessageResponse = buildDummyResponse(false);
        doReturn(chatPostMessageResponse).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendErrorTopicMessage(buildDummyFailedMessages());
        verify(logger).error(String.format("Error topic message sent but received response: %s",
                chatPostMessageResponse.getError()));
    }

    @Test
    void testFailedSlackErrorTopicMessageWithIOException() throws IOException, SlackApiException {
        IOException io = new IOException(null, null);
        doThrow(io).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        slackMessagingServiceImpl.sendErrorTopicMessage(buildDummyFailedMessages());
        verify(logger).errorContext("Error topic slack error message not sent", io);
    }

    @Test
    void testSuccessfulDeserializationErrorMessage() throws IOException, SlackApiException {
        doReturn(buildDummyResponse(true)).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        String errorMessage = "Message rejected - Failed to deserialize message - topic: Test-topic, partition: 1, offset: 1";
        slackMessagingServiceImpl.sendRejectedErrorMessage(errorMessage);
        verify(logger).info(String.format("Message rejected error message sent to: %s", SLACK_CHANNEL));
    }

    @Test
    void testFailedSlackDeserializationErrorMessage() throws IOException, SlackApiException {
        ChatPostMessageResponse chatPostMessageResponse = buildDummyResponse(false);
        doReturn(chatPostMessageResponse).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        String errorMessage = "Message rejected - Failed to deserialize message - topic: Test-topic, partition: 1, offset: 1";
        slackMessagingServiceImpl.sendRejectedErrorMessage(errorMessage);
        verify(logger).error(String.format("Message rejected error message sent but received response: %s",
                chatPostMessageResponse.getError()));
    }

    @Test
    void testFailedSlackDeserializationErrorMessageWithIOException() throws IOException, SlackApiException {
        IOException io = new IOException(null, null);
        doThrow(io).when(slackMessagingServiceImpl).postSlackMessage(any(), any());
        String errorMessage = "Message rejected - Failed to deserialize message - topic: Test-topic, partition: 1, offset: 1";
        slackMessagingServiceImpl.sendRejectedErrorMessage(errorMessage);
        verify(logger).errorContext("Message rejected error slack error message not sent", io);
    }

    private List<String> buildDummyFailedMessages() {
        List<String> failedMessageIds = new ArrayList<>();
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
