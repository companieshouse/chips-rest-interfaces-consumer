package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface ErrorConsumer {
    void readAndProcessErrorTopic(@Payload ChipsRestInterfacesSend data,
                                 @Headers MessageHeaders headers);

}
