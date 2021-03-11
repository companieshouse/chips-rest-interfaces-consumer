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

import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_GROUP_ID;
import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_OFFSET;
import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_PARTITION;

@Service
@ConditionalOnProperty(prefix = "feature", name = "errorMode", havingValue = "false")
public class MainConsumerImpl implements MainConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    private final SlackMessagingService slackMessagingService;

    @Value("${FEATURE_FLAG_SLACK_MESSAGES_020321}")
    private boolean doSendSlackMessages;

    @Value("${BATCH_FAILURE_RETRY_SLEEP_MS:1000}")
    private long batchFailureRetrySleepMs;

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
        logMap.put(KEY_GROUP_ID, groupId);
        logMap.put(KEY_PARTITION, partition);
        logMap.put(KEY_OFFSET, offset);

        logger.debugContext(messageId, acknowledgment.toString(), logMap);

        data.setAttempt(0);
        processMessage(groupId, data, offset, partition);

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

        boolean isBatchOk = true;

        for (int i = 0; i < messages.size(); i++) {
            var message = messages.get(i);
            var messageID = message.getMessageId();
            var partition = partitions.get(i);
            var offset = offsets.get(i);

            try {
                if (!processMessage(groupId, message, offset, partition)) {
                    failedMessageIds.add(messageID);
                }
            } catch (Exception e) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put(KEY_GROUP_ID, groupId);
                logMap.put(KEY_PARTITION, partition);
                logMap.put(KEY_OFFSET, offset);

                logger.errorContext(messageID, e, logMap);

                failedMessageIds.add(messageID);

                acknowledgment.nack(i, batchFailureRetrySleepMs);
                logger.infoContext(messageID, String.format("Committed messages in batch up to offset %s", offset), logMap);
                isBatchOk = false;
                break;
            }
        }

        if (isBatchOk) {
            acknowledgment.acknowledge();
            logger.debug(String.format("%s, acknowledged batch of %s messages", groupId, batchSize));
        }

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
     */
    private boolean processMessage(String consumerId,
                                   ChipsRestInterfacesSend data,
                                   Long offset,
                                   Integer partition) {

        var messageId = data.getMessageId();
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(KEY_GROUP_ID, consumerId);
        logMap.put(KEY_PARTITION, partition);
        logMap.put(KEY_OFFSET, offset);

        logger.infoContext(messageId, String.format("%s: Consumed Message from Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
        logger.infoContext(messageId, String.format("received data='%s'", data), logMap);

        boolean isSuccessful = messageProcessorService.processMessage(consumerId, data);

        logger.infoContext(messageId, String.format("%s: Finished Processing Message from Partition: %s, Offset: %s", consumerId, partition, offset), logMap);
        return isSuccessful;
    }
}
