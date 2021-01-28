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
import uk.gov.companieshouse.service.ServiceException;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

    private static final String SEND_FAILURE_MESSAGE = "Error sending message id %s to chips";

    @Value("${MAX_RETRY_ATTEMPTS}")
    private int maxRetryAttempts;

    @Value("${kafka.retry.topic}")
    private String retryTopicName;

    @Value("${kafka.error.topic}")
    private String errorTopicName;

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void processMessage(ChipsRestInterfacesSend message) throws ServiceException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Message", message.getData());
        try {
            chipsRestClient.sendToChips(message);
        } catch (HttpStatusCodeException hsce) {
            logMap.put("HTTP Status Code", hsce.getStatusCode().toString());
            handleFailedMessage(message, hsce, logMap);
        } catch (Exception e) {
            handleFailedMessage(message, e, logMap);
        }
    }

    private void handleFailedMessage(ChipsRestInterfacesSend message, Exception e, Map<String, Object> logMap) throws ServiceException {
        var messageId = message.getMessageId();
        logger.error(String.format(SEND_FAILURE_MESSAGE, messageId), e, logMap);

        var attempts = message.getAttempt();
        logger.info(String.format("Attempt %s failed for message id %s", attempts, messageId), logMap);

        if (attempts < maxRetryAttempts) {
            message.setAttempt(attempts + 1);
            messageProducer.writeToTopic(message, retryTopicName);
        } else {
            logger.error(String.format("Maximum retry attempts %s reached for message id %s", maxRetryAttempts, messageId), e, logMap);
            messageProducer.writeToTopic(message, errorTopicName);
        }
    }
}
