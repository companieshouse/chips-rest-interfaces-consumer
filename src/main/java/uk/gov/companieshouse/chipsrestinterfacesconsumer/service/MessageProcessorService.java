package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.util.List;

public interface MessageProcessorService {
    void processMessage(String consumerId, ChipsRestInterfacesSend message, List<String> failedMessages);
}
