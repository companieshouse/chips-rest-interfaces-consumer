package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MessageRejectionService {
    void handleRejectedMessage(Exception exception, ConsumerRecord<?, ?> datum);
}
