package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.util.List;
import java.util.Optional;

public interface MessageProcessorService {
    void processMessage(String consumerId, ChipsRestInterfacesSend message, Optional<List<String>> failedMessageOpt);
}
