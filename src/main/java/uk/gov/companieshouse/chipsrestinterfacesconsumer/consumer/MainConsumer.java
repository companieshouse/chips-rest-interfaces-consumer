package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.util.List;

public interface MainConsumer {

    void readAndProcessMainTopic(@Payload ChipsRestInterfacesSend data,
                                 @Header(KafkaHeaders.OFFSET) Long offset,
                                 @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                                 @Header(KafkaHeaders.GROUP_ID) String groupId
    );

    void readAndProcessRetryTopic(@Payload List<ChipsRestInterfacesSend> messages,
                                  @Header(KafkaHeaders.OFFSET) List<Long> offsets,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                                  @Header(KafkaHeaders.GROUP_ID) String groupId
    );
}
