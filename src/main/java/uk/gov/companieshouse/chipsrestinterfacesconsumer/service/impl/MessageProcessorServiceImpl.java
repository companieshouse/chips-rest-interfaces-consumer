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
        var messageId = message.getMessageId();
        try {
            chipsRestClient.sendToChips(message);
        } catch (HttpStatusCodeException hsce) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("HTTP Status Code", hsce.getStatusCode());
            logger.error(String.format(SEND_FAILURE_MESSAGE, messageId), hsce, logMap);
            handleFailedMessage(message, hsce);
        } catch (Exception e) {
            logger.error(String.format(SEND_FAILURE_MESSAGE, messageId), e);
            handleFailedMessage(message, e);
        }
    }

    private void handleFailedMessage(ChipsRestInterfacesSend message, Exception e) throws ServiceException {
        var messageId = message.getMessageId();
        var attempts = message.getAttempt();
        logger.info(String.format("Attempt %s failed for message id %s", attempts, messageId));

        if (attempts < maxRetryAttempts) {
            message.setAttempt(attempts + 1);
            messageProducer.writeToTopic(message, retryTopicName);
        } else {
            logger.error(String.format("Maximum retry attempts %s reached for message id %s", maxRetryAttempts, messageId), e);
            messageProducer.writeToTopic(message, errorTopicName);
        }
    }
}
