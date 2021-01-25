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
        } catch (HttpClientErrorException hcee) {
            logMap.put("HTTP Status Code", hcee.getStatusCode());
            handleFailedMessage(message, hcee, logMap);
        } catch (Exception e) {
            handleFailedMessage(message, e, logMap);
        }
    }

    private void handleFailedMessage(ChipsRestInterfacesSend message, Exception e, Map<String, Object> logMap) throws ServiceException {
        logger.error(SEND_FAILURE_MESSAGE, e, logMap);

        var attempts = message.getAttempt();
        if (attempts < maxRetryAttempts) {
            attempts++;
            logger.info(String.format("Placing message on RETRY topic for attempt %s", attempts), logMap);
            message.setAttempt(attempts);
            retryMessageProducer.writeToTopic(message);
        } else {
            logger.error(String.format("Maximum retry attempts %s reached, moving message to ERROR topic", maxRetryAttempts), e, logMap);
            // TODO move to Error topic
        }
    }
}
