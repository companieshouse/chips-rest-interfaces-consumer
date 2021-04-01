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
        String errorMessageDetails = buildMessage(datum.topic(), datum.partition(), datum.offset());
        if (exception.getCause() instanceof DeserializationException) {
             logger.error(errorMessageDetails, exception);
             sendToSlack(errorMessageDetails);
        } else {
            logger.error(String.format("Kafka consumer message exception - %s", errorMessageDetails), exception);
        }
    }

    private void sendToSlack(String errorMessageDetails) {
        String deserializationFailureMessage = String.format("Failed to deserialize message - %s", errorMessageDetails);
        slackMessagingService.sendDeserializationErrorMessage(deserializationFailureMessage);
    }

    private String buildMessage(String topic, int partition, long offset) {
        return "topic: " + topic + ", partition: " + partition + ", offset: " + offset;
    }
}
