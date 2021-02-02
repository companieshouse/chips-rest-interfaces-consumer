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
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
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

    @Autowired
    private ConsumerConfig consumerConfig;

    @Override
    public void processMessage(ChipsRestInterfacesSend message, boolean isErrorAttempt) throws ServiceException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Message", message.getData());
        try {
            chipsRestClient.sendToChips(message);
        } catch (HttpStatusCodeException hsce) {
            logMap.put("HTTP Status Code", hsce.getStatusCode().toString());
            handleFailedMessage(message, hsce, logMap, isErrorAttempt);
        } catch (Exception e) {
            handleFailedMessage(message, e, logMap, isErrorAttempt);
        }
    }

    private void handleFailedMessage(ChipsRestInterfacesSend message, Exception e, Map<String, Object> logMap, boolean isErrorAttempt) throws ServiceException {
        var messageId = message.getMessageId();
        logger.error(String.format(SEND_FAILURE_MESSAGE, messageId), e, logMap);

        if (isErrorAttempt) {
            message.setAttempt(1);
            messageProducer.writeToTopic(message, consumerConfig.retryTopic());
            return;
        }

        var attempts = message.getAttempt();
        logger.info(String.format("Attempt %s failed for message id %s", attempts, messageId), logMap);

        if (attempts < maxRetryAttempts) {
            message.setAttempt(attempts + 1);
            messageProducer.writeToTopic(message, consumerConfig.retryTopic());
        } else {
            logger.error(String.format("Maximum retry attempts %s reached for message id %s", maxRetryAttempts, messageId), e, logMap);
            messageProducer.writeToTopic(message, consumerConfig.errorTopic());
        }
    }
}
