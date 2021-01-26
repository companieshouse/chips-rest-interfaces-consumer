package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.MessageProducer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.Collections;
import java.util.Map;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

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
        Map<String, Object> logMap = Collections.singletonMap("Message", message.getData());
        logger.info("About to send message to Chips", logMap);
        try {
            chipsRestClient.sendToChips(message);
        } catch (RestClientException restClientException) {
            var attempts = message.getAttempt();
            if (attempts < maxRetryAttempts) {
                logger.error("Error sending message to chips, will place on retry queue", restClientException, logMap);
                message.setAttempt(attempts + 1);
                messageProducer.writeToTopic(message, retryTopicName);
            } else {
                logger.error("Error max attempts reached", restClientException, logMap);
                messageProducer.writeToTopic(message, errorTopicName);
            }
        }
    }
}
