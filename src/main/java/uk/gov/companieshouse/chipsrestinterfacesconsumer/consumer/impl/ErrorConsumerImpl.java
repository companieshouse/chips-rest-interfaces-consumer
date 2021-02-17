package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.ErrorConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "feature", name = "errorMode", havingValue = "true")
public class ErrorConsumerImpl implements ErrorConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    @Autowired
    public ErrorConsumerImpl(ApplicationLogger logger, MessageProcessorService messageProcessorService) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
    }

    @PostConstruct
    void init() {
        logger.info("***** Application started in error processing mode *****");
    }

    /**
     *
     * Creates a container using the containerFactory argument to handle any messages retrieved from kafka
     *
     * @param data The deserialized message from Kafka
     * @param offset The offset of {@code data}
     * @param partition The partition of {@code data}
     * @param groupId The group id of the consumer
     */
    @Override
    @KafkaListener(topics = "${kafka.error.topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "error-group")
    public void readAndProcessErrorTopic(@Payload ChipsRestInterfacesSend data,
                                         @Header(KafkaHeaders.OFFSET) Long offset,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                                         @Header(KafkaHeaders.GROUP_ID) String groupId){

        var messageId = data.getMessageId();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Group Id", groupId);
        logMap.put("Partition", partition);
        logMap.put("Offset", offset);

        logger.infoContext(messageId, String.format("%s: Consumed Message from Partition: %s, Offset: %s", groupId, partition, offset), logMap);
        logger.infoContext(messageId, String.format("received data='%s'", data), logMap);

        messageProcessorService.processMessage("error-consumer", data);
        logger.infoContext(messageId, String.format("%s: Finished Processing Message from Partition: %s, Offset: %s", groupId, partition, offset), logMap);
    }
}
