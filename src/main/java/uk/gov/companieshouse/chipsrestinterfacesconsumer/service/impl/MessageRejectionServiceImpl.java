package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageRejectionService;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

@Service
public class MessageRejectionServiceImpl implements MessageRejectionService {

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private SlackMessagingService slackMessagingService;

    @Override
    public void handleRejectedMessage(Exception exception, ConsumerRecord<?, ?> datum) {
        String errorMessage = buildMessage(datum.topic(), datum.partition(), datum.offset());
        if (exception.getCause() instanceof DeserializationException) {
            errorMessage = String.format("Message rejected - Failed to deserialize message - %s", errorMessage);
        } else {
            errorMessage = String.format("Message rejected - Kafka consumer message exception - %s", errorMessage);
        }
        logger.error(errorMessage, exception);
        slackMessagingService.sendRejectedErrorMessage(errorMessage);
    }

    private String buildMessage(String topic, int partition, long offset) {
        return "topic: " + topic + ", partition: " + partition + ", offset: " + offset;
    }
}
