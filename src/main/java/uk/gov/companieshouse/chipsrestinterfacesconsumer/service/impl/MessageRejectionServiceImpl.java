package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
    public void handleRejectedMessageBatch(Exception exception, ConsumerRecords<?, ?> data) {

        StringBuilder errorMessageDetails = new StringBuilder();
        data.forEach((datum)-> {
            errorMessageDetails.append(
                buildMessage(datum.topic(), datum.partition(), datum.offset()));
            errorMessageDetails.append("\n");

        });

        if (exception.getCause() instanceof DeserializationException) {
            sendToSlack(errorMessageDetails.toString());
        } else {
          logger.error(String.format("Kafka message exception - %s - %s", exception.getMessage(), errorMessageDetails));
        }
    }

    @Override
    public void handleRejectedMessage(Exception exception, ConsumerRecord<?, ?> datum) {
        String errorMessageDetails = buildMessage(datum.topic(), datum.partition(), datum.offset());

        if (exception.getCause() instanceof DeserializationException) {
            sendToSlack(errorMessageDetails);
        } else {
            logger.error(String.format("Kafka message exception - %s - %s", exception.getMessage(), errorMessageDetails));
        }
    }

    private void sendToSlack(String errorMessageDetails) {
        String deserializationFailureMessage = String.format("Failed to deserialize message - %s", errorMessageDetails);
        logger.error(deserializationFailureMessage);
        slackMessagingService.sendDeserializationErrorMessage(deserializationFailureMessage);
    }

    private String buildMessage(String topic, int partition, long offset) {
          return "topic: " + topic + ", partition: " + partition + ", offset: " + offset;
    }
}
