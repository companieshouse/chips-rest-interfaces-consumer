package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.impl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.MessageProducer;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_MESSAGE;
import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_OFFSET;
import static uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger.KEY_PARTITION;

@Service
public class MessageProducerImpl implements MessageProducer {

    private final ApplicationLogger logger;

    private final KafkaTemplate<String, ChipsRestInterfacesSend> kafkaTemplate;

    public MessageProducerImpl(ApplicationLogger logger, KafkaTemplate<String, ChipsRestInterfacesSend> kafkaTemplate) {
        this.logger = logger;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void writeToTopic(ChipsRestInterfacesSend chipsMessage, String topicName) {
        var messageId = chipsMessage.getMessageId();
        var future = kafkaTemplate.send(topicName, chipsMessage);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, ChipsRestInterfacesSend> result) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put(KEY_OFFSET, result.getRecordMetadata().offset());
                logMap.put(KEY_PARTITION, result.getRecordMetadata().partition());

                logger.infoContext(messageId, "Sent messageId " + messageId + " to topic " + topicName, logMap);
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.errorContext(messageId, "Unable to send message due to : " + ex.getMessage(), new Exception(ex));
            }
        });
    }
}