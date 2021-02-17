package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.MessageProducer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

    private static final String SEND_FAILURE_MESSAGE = "Error sending this message to chips";

    @Value("${MAX_RETRY_ATTEMPTS}")
    private int maxRetryAttempts;

    @Value("${kafka.retry.topic}")
    private String retryTopicName;

    @Value("${kafka.error.topic}")
    private String errorTopicName;

    @Value("${RUN_APP_IN_ERROR_MODE:false}")
    private boolean runAppInErrorMode;

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void processMessage(String consumerId, ChipsRestInterfacesSend message) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Message", message.getData());
        logMap.put("Message Consumer ID", consumerId);
        try {
            chipsRestClient.sendToChips(message, consumerId);
        } catch (HttpStatusCodeException hsce) {
            logMap.put("HTTP Status Code", hsce.getStatusCode().toString());
            handleFailedMessage(message, hsce, logMap);
        } catch (Exception e) {
            handleFailedMessage(message, e, logMap);
        }
    }

    private void handleFailedMessage(ChipsRestInterfacesSend message, Exception e, Map<String, Object> logMap) {
        var messageId = message.getMessageId();
        logger.errorContext(messageId, SEND_FAILURE_MESSAGE, e, logMap);

        if (runAppInErrorMode) {
            message.setAttempt(1);
            messageProducer.writeToTopic(message, retryTopicName);
            return;
        }

        var attempts = message.getAttempt();
        logger.infoContext(messageId, String.format("Attempt %s failed for this message", attempts), logMap);

        if (attempts < maxRetryAttempts) {
            message.setAttempt(attempts + 1);
            messageProducer.writeToTopic(message, retryTopicName);
        } else {
            logger.errorContext(messageId, String.format("Maximum retry attempts %s reached for this message", maxRetryAttempts), e, logMap);
            messageProducer.writeToTopic(message, errorTopicName);
        }
    }
}
