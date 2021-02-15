package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.impl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.producer.MessageProducer;

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
        var future = kafkaTemplate.send(topicName, chipsMessage);

        future.addCallback(new ListenableFutureCallback<>() {

            @Override
            public void onSuccess(SendResult<String, ChipsRestInterfacesSend> result) {
                logger.info("Sent message=[" + chipsMessage +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Unable to send message=["
                        + chipsMessage + "] due to : " + ex.getMessage(), new Exception(ex));
            }
        });
    }
}