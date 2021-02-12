package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.service.ServiceException;

public interface MessageProducer {
    void writeToTopic(ChipsRestInterfacesSend payload, String topic) throws ServiceException;
}
