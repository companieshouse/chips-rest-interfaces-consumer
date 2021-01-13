package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;

import java.util.Collections;
import java.util.Map;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Autowired
    private ApplicationLogger logger;

    @Override
    public void processMessage(ChipsRestInterfacesSend message) {
        Map<String, Object> logMap = Collections.singletonMap("Message", message);
        logger.info("About to send message to Chips", logMap);
        chipsRestClient.sendToChips(message);
    }
}
