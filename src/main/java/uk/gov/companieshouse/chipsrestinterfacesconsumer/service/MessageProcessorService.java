package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface MessageProcessorService {
    void processMessage(ChipsRestInterfacesSend message) throws ServiceException;
}
