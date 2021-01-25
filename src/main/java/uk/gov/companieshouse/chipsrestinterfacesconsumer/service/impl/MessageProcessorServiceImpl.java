package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.MessageProducer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.HashMap;
import java.util.Map;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

    private static final String SEND_FAILURE_MESSAGE = "Error sending message to chips";

    @Value("${MAX_RETRY_ATTEMPTS}")
    private int maxRetryAttempts;

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private MessageProducer retryMessageProducer;

    @Override
    public void processMessage(ChipsRestInterfacesSend message) throws ServiceException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Message", message.getData());
        logger.info("About to send message to Chips", logMap);
        try {
            chipsRestClient.sendToChips(message);
        } catch (HttpClientErrorException httpStatusCodeException) {
            logMap.put("HTTP Status Code", httpStatusCodeException.getStatusCode());
            logMap.put("HTTP Status Text", httpStatusCodeException.getStatusText());
            logger.error(SEND_FAILURE_MESSAGE, httpStatusCodeException, logMap);
        } catch (Exception exception) {
            logger.error(SEND_FAILURE_MESSAGE, exception, logMap);
        } finally {
            moveToCorrectTopic(message, logMap);
        }
    }

    private void moveToCorrectTopic(ChipsRestInterfacesSend message, Map<String, Object> logMap) throws ServiceException {
        var attempts = message.getAttempt();
        if (attempts < maxRetryAttempts) {
            logger.info("Moving message to RETRY topic", logMap);
            message.setAttempt(attempts + 1);
            retryMessageProducer.writeToTopic(message);
        } else {
            logger.info("Maximum retry attempts reached, moving message to ERROR topic", logMap);
            // TODO move to Error topic
        }
    }
}
