package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.service.ServiceException;

public interface MessageProducer {
    void writeToQueue(ChipsKafkaMessage message) throws ServiceException;
}
