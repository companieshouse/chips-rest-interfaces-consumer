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

@Service
public class SlackMessagingServiceImpl implements SlackMessagingService {

    private final ApplicationLogger logger;

    @Value("${SLACK_CHANNEL}")
    private String slackChannel;

    @Value("${SLACK_ACCESS_TOKEN}")
    private String slackAccessToken;

    @Value("${SLACK_ERROR_MESSAGE}")
    private String slackErrorMessage;

    @Value("${RUN_APP_IN_ERROR_MODE}")
    private boolean inErrorMode;

    @Autowired
    public SlackMessagingServiceImpl(ApplicationLogger logger) {
        this.logger = logger;
    }

    @Override
    public void sendMessage(String kafkaMessageId) {

        try {
            String mode = (inErrorMode)? "error" : "normal";
            Slack slack = Slack.getInstance();
            MethodsClient methods = slack.methods(slackAccessToken);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(slackChannel)
                    .text(String.format(slackErrorMessage, mode, kafkaMessageId))
                    .build();

            ChatPostMessageResponse response = methods.chatPostMessage(request);
            if(response.isOk()) {
                logger.infoContext(kafkaMessageId, String.format("Message sent to: %s", slackChannel));
            } else {
                logger.infoContext(kafkaMessageId, String.format("Error message sent but received response: %s", response.getError()));
            }
        } catch(IOException | SlackApiException e) {
            logger.errorContext(kafkaMessageId, "Slack error message not sent", e);
        }
    }
}
