package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public interface MessageRejectionService {

    void handleRejectedMessageBatch(Exception exception, ConsumerRecords<?, ?> data);
    void handleRejectedMessage(Exception exception, ConsumerRecord<?, ?> datum);
}
