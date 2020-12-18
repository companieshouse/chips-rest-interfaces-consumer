package uk.gov.companieshouse.chipsrestinterfacesconsumer.processor;

import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import java.util.Collection;

public interface MessageConsumer {
    Collection<ChipsKafkaMessage> read();
}
