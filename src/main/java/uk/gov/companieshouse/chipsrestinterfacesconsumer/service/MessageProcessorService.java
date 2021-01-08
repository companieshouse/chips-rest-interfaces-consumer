package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

public interface MessageProcessorService {
    void processMessage(ChipsKafkaMessage message);
}
