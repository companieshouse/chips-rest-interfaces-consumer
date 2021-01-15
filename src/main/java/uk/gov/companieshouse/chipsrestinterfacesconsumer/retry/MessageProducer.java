package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.service.ServiceException;

public interface MessageProducer {
    void writeToTopic(ChipsRestInterfacesSend message) throws ServiceException;
}
