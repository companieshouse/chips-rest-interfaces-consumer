package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public interface ErrorConsumer {
    void readAndProcessErrorTopic(@Payload ChipsRestInterfacesSend data,
                                  Acknowledgment acknowledgment,
                                  @Header(KafkaHeaders.OFFSET) Long offset,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                                  @Header(KafkaHeaders.GROUP_ID) String groupId);

}
