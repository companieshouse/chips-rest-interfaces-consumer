package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;

import java.util.Collection;

@Service
public class MessageProcessorServiceImpl implements MessageProcessorService {

    @Autowired
    private ChipsRestClient chipsRestClient;

    @Override
    public void processMessages(Collection<ChipsKafkaMessage> messages) {
        messages.parallelStream().forEach(chipsKafkaMessage -> chipsRestClient.sendToChips(chipsKafkaMessage));
    }
}
