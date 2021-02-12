package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface MainConsumer {
    void readAndProcessMainTopic(@Payload ChipsRestInterfacesSend data,
                                 @Headers MessageHeaders headers);
    void readAndProcessRetryTopic(@Payload ChipsRestInterfacesSend data,
                                 @Headers MessageHeaders headers);
}
