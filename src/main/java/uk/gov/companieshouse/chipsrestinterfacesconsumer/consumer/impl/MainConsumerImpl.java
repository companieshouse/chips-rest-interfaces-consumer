package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MainConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("!error-mode")
public class MainConsumerImpl implements MainConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    @Autowired
    public MainConsumerImpl(ApplicationLogger logger, MessageProcessorService messageProcessorService) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
    }

    @Override
    @KafkaListener(topics = "${kafka.main.topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "main-group")
    public void readAndProcessMainTopic(@Payload ChipsRestInterfacesSend data,
                                        @Header(KafkaHeaders.OFFSET) Long offset,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                                        @Header(KafkaHeaders.GROUP_ID) String groupId
    ){
        processMessage(groupId, data, offset, partition);
    }

    @Override
    @KafkaListener(topics = "${kafka.retry.topic}", containerFactory = "kafkaRetryListenerContainerFactory", groupId = "retry-group")
    public void readAndProcessRetryTopic(@Payload List<ChipsRestInterfacesSend> messages,
                                         @Header(KafkaHeaders.OFFSET) List<Long> offsets,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                                         @Header(KafkaHeaders.GROUP_ID) String groupId
    ){

        logger.debug(String.format("%s, received %s messages", groupId, messages.size()));
        for (int i = 0; i < messages.size(); i++) {
            processMessage(groupId, messages.get(i), offsets.get(i), partitions.get(i));
        }

    }

    private void processMessage(String consumerId, ChipsRestInterfacesSend data, Long offset, Integer partition) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Group Id", consumerId);
        logMap.put("Partition", partition);
        logMap.put("Offset", offset);

        try {
            logger.info(String.format("%s: Consumed Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
            logger.info(String.format("received data='%s'", data), logMap);
            messageProcessorService.processMessage(consumerId, data);
        } catch (ServiceException se) {
            logger.error("Failed to process message", se);
        } finally {
            logger.info(String.format("%s: Finished Processing Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
        }
    }
}
