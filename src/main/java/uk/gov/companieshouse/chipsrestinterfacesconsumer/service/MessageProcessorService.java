package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.service.ServiceException;

public interface MessageProcessorService {
    void processMessage(ChipsKafkaMessage message) throws ServiceException;
}
