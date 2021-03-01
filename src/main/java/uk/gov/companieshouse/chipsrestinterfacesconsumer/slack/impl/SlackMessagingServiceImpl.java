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

    @Value("${RUN_APP_IN_ERROR_MODE}")
    private boolean inErrorMode;

    @Autowired
    public SlackMessagingServiceImpl(ApplicationLogger logger) {
        this.logger = logger;
    }

    @Override
    public void sendMessage(List<String> failedMessageIds) {

        try {
            String slackErrorMessage = buildMessage(failedMessageIds);
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(slackAccessToken);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(slackChannel)
                    .text(slackErrorMessage)
                    .build();

            ChatPostMessageResponse response = postSlackMessage(methods, request);
            if(response.isOk()) {
                logger.info(String.format("Message sent to: %s", slackChannel));
            } else {
                logger.error(String.format("Error message sent but received response: %s", response.getError()));
            }
        } catch(IOException | SlackApiException e) {
            logger.errorContext("Slack error message not sent", e);
        } finally {
            failedMessageIds.clear();
        }
    }

    ChatPostMessageResponse postSlackMessage(
            MethodsClient methods,
            ChatPostMessageRequest request) throws IOException, SlackApiException {
        return methods.chatPostMessage(request);
    }

    private String buildMessage(List<String> failedMessageIds) {
        String mode = (inErrorMode)? "error" : "normal";

        StringBuilder failedSb = new StringBuilder();
        failedSb.append(String.format("In %s mode - Unable to send message with ids: %n", mode));

        for(String failedMessageId : failedMessageIds){
            failedSb.append(failedMessageId + "\n");
        }

        return failedSb.toString();
    }
}
