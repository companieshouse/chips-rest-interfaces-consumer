package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.impl;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import java.io.IOException;
import java.util.List;

@Service
public class SlackMessagingServiceImpl implements SlackMessagingService {

    private final ApplicationLogger logger;

    @Value("${SLACK_CHANNEL}")
    private String slackChannel;

    @Value("${SLACK_ACCESS_TOKEN}")
    private String slackAccessToken;

    @Autowired
    public SlackMessagingServiceImpl(ApplicationLogger logger) {
        this.logger = logger;
    }

    @Override
    public void sendErrorTopicMessage(List<String> failedMessageIds) {

        StringBuilder failedSb = new StringBuilder();
        failedSb.append("Unable to send messages with ids: ");
        failedSb.append("\n");

        for (String failedMessageId : failedMessageIds) {
            failedSb.append(failedMessageId);
            failedSb.append("\n");
        }

        sendMessage(failedSb.toString(), "Error topic");
        failedMessageIds.clear();
    }

    @Override
    public void sendDeserializationErrorMessage(String deserializationFailureMessage) {
       sendMessage(deserializationFailureMessage, "Deserialization error");
    }

    ChatPostMessageResponse postSlackMessage(
            MethodsClient methods,
            ChatPostMessageRequest request) throws IOException, SlackApiException {
        return methods.chatPostMessage(request);
    }

    private void sendMessage(String slackMessage, String messageType) {
        try {
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(slackAccessToken);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(slackChannel)
                    .text(slackMessage)
                    .build();

            ChatPostMessageResponse response = postSlackMessage(methods, request);
            if (response.isOk()) {
                logger.info(String.format("%s message sent to: %s", messageType, slackChannel));
            } else {
                logger.error(String.format("%s message sent but received response: %s", messageType, response.getError()));
            }
        } catch(IOException | SlackApiException e) {
            logger.errorContext(String.format("%s slack error message not sent", messageType), e);
        }
    }
}
