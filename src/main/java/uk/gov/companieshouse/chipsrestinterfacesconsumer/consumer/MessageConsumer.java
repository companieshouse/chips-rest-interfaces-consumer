package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import java.util.Collection;

public interface MessageConsumer {
    void readAndProcess();
}
