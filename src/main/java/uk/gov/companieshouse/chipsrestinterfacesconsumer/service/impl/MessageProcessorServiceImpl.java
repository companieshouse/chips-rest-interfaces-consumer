package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private MessageProducer retryMessageProducer;

    @Override
    public void processMessage(ChipsRestInterfacesSend message) throws ServiceException {
        Map<String, Object> logMap = Collections.singletonMap("Message", message);
        logger.info("About to send message to Chips", logMap);
        try {
            chipsRestClient.sendToChips(message);
        } catch (RestClientException restClientException) {
            logger.error("Error sending message to chips, will place on retry queue", restClientException, logMap);
            retryMessageProducer.writeToTopic(message);
        }
    }
}
