package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.service.ServiceException;

public interface MessageProcessorService {
    void processMessage(String consumerId, ChipsRestInterfacesSend message) throws ServiceException;
}
