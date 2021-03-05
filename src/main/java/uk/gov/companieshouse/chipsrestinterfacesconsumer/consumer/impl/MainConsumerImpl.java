package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MainConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "feature", name = "errorMode", havingValue = "false")
public class MainConsumerImpl implements MainConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    private final SlackMessagingService slackMessagingService;

    @Value("${FEATURE_FLAG_SLACK_MESSAGES_020321}")
    private boolean doSendSlackMessages;

    @Autowired
    public MainConsumerImpl(ApplicationLogger logger,
                            MessageProcessorService messageProcessorService,
                            SlackMessagingService slackMessagingService) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
        this.slackMessagingService = slackMessagingService;
    }

    @PostConstruct
    void init() {
        logger.info("***** Application started in normal processing mode *****");
    }

    /**
     * Creates a container using the containerFactory argument to handle any messages retrieved from kafka
     *
     * @param data The deserialized message from Kafka
     * @param acknowledgment The {@link Acknowledgment} to be called to commit the offset of {@code data}
     * @param offset The offset of {@code data}
     * @param partition The partition of {@code data}
     * @param groupId The group id of the consumer
     */
    @Override
    @KafkaListener(topics = "${kafka.main.topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "main-group")
    public void readAndProcessMainTopic(@Payload ChipsRestInterfacesSend data,
                                        Acknowledgment acknowledgment,
                                        @Header(KafkaHeaders.OFFSET) Long offset,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION_ID) Integer partition,
                                        @Header(KafkaHeaders.GROUP_ID) String groupId
    ){
        var messageId = data.getMessageId();

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Group Id", groupId);
        logMap.put("Partition", partition);
        logMap.put("Offset", offset);

        logger.debugContext(messageId, acknowledgment.toString(), logMap);

        data.setAttempt(0);
        processMessage(groupId, data, offset, partition, null);

        acknowledgment.acknowledge();
        logger.debugContext(messageId, String.format("Acknowledged message %s", messageId), logMap);
    }

    /**
     * Creates a container using the containerFactory argument to handle any messages retrieved from kafka
     *
     * @param messages A list of deserialized messages from Kafka
     * @param acknowledgment The {@link Acknowledgment} to be called to commit the offsets of all {@code messages} in the batch
     * @param offsets A list of the offsets for the messages in {@code messages}
     * @param partitions A list of the partitions for the messages in {@code messages}
     * @param groupId The group id of the consumer
     */
    @Override
    @KafkaListener(topics = "${kafka.retry.topic}", containerFactory = "kafkaRetryListenerContainerFactory", groupId = "retry-group")
    public void readAndProcessRetryTopic(@Payload List<ChipsRestInterfacesSend> messages,
                                         Acknowledgment acknowledgment,
                                         @Header(KafkaHeaders.OFFSET) List<Long> offsets,
                                         @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
                                         @Header(KafkaHeaders.GROUP_ID) String groupId
    ){
        var batchSize = messages.size();
        logger.debug(String.format("%s, received %s messages", groupId, batchSize));

        List<String> failedMessageIds = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            processMessage(groupId, messages.get(i), offsets.get(i), partitions.get(i), failedMessageIds);
        }

        acknowledgment.acknowledge();
        logger.debug(String.format("%s, acknowledged batch of %s messages", groupId, batchSize));

        if (doSendSlackMessages && !failedMessageIds.isEmpty()) {
            slackMessagingService.sendMessage(failedMessageIds);
        }
    }

    /**
     * Delegates the processing of the message to the {@code messageProcessorService}
     * and handles any unexpected errors
     *
     * @param consumerId the id of the consumer calling this method
     * @param data a deserialized message from kafka
     * @param offset The offset of {@code data}
     * @param partition The partition of {@code data}
     * @param failedMessageIds collects the ids of failed Kafka messages
     */
    private void processMessage(String consumerId,
                                ChipsRestInterfacesSend data,
                                Long offset,
                                Integer partition,
                                List<String> failedMessageIds) {

        var messageId = data.getMessageId();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("Group Id", consumerId);
        logMap.put("Partition", partition);
        logMap.put("Offset", offset);

        logger.infoContext(messageId, String.format("%s: Consumed Message from Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
        logger.infoContext(messageId, String.format("received data='%s'", data), logMap);

        boolean isSuccessful = messageProcessorService.processMessage(consumerId, data);
        if (failedMessageIds != null && !isSuccessful) {
            failedMessageIds.add(messageId);
        }

        logger.infoContext(messageId, String.format("%s: Finished Processing Message from Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
    }
}
