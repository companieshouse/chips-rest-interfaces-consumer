package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface MessageProducer {
    void writeToTopic(ChipsRestInterfacesSend payload, String topic);
}
