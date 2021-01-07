package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.util.Collection;

public interface MessageProcessorService {
    void processMessages(Collection<ChipsKafkaMessage> messages);
}
