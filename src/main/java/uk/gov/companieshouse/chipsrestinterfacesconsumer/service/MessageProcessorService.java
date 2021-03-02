package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface MessageProcessorService {
    boolean processMessage(String consumerId, ChipsRestInterfacesSend message);
}
