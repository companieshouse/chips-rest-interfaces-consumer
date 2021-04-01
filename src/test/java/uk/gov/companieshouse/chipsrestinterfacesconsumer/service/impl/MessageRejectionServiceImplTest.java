package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.serializer.DeserializationException;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.slack.SlackMessagingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageRejectionServiceImplTest {

    private static final String TOPIC = "test-topic";

    @Mock
    private ApplicationLogger logger;

    @Mock
    private SlackMessagingService slackMessagingService;

    @InjectMocks
    private MessageRejectionServiceImpl messageRejectionService;


    @Test
    public void testDeserializeExceptionForMainConsumer() {
        Exception exception = new Exception(new DeserializationException("", new byte[1], false, new SerializationException()));
        ConsumerRecord<?, ?> datum = new ConsumerRecord<>(TOPIC,1,1L, "", "");
        messageRejectionService.handleRejectedMessage(exception, datum);
        String deserializationErrorMessage = "Failed to deserialize message - " + buildSingleMessage(TOPIC,1,1L);
        verify(slackMessagingService, times(1)).sendDeserializationErrorMessage(deserializationErrorMessage);
    }

    @Test
    public void testDeserializeExceptionForRetryBatchConsumer() {
        Exception exception = new Exception(new DeserializationException("", new byte[1], false, new SerializationException()));
        ConsumerRecord<String, String> datum1 = new ConsumerRecord<>(TOPIC,1,1L, "", "");
        ConsumerRecord<String, String> datum2 = new ConsumerRecord<>(TOPIC,1,2L, "", "");
        ConsumerRecord<String, String> datum3 = new ConsumerRecord<>(TOPIC,1,3L, "", "");

        List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
        recordList.add(datum1);
        recordList.add(datum2);
        recordList.add(datum3);

        TopicPartition partition = new TopicPartition(TOPIC, 1);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
        records.put(partition, recordList);

        ConsumerRecords<?, ?> data = new ConsumerRecords<>(records);
        messageRejectionService.handleRejectedMessageBatch(exception, data);

        String deserializationErrorMessage =
                "Failed to deserialize message - " + buildMultiMessage(data);
        verify(slackMessagingService, times(1)).sendDeserializationErrorMessage(deserializationErrorMessage);
    }

    @Test
    public void testNonDeserializeExceptionForMainConsumer() {
        Exception exception = new Exception();
        ConsumerRecord<?, ?> datum = new ConsumerRecord<>(TOPIC,1,1L, "", "");
        messageRejectionService.handleRejectedMessage(exception, datum);
        String deserializationErrorMessage = "Failed to deserialize message - " + buildSingleMessage(TOPIC,1,1L);
        verify(slackMessagingService, never()).sendDeserializationErrorMessage(deserializationErrorMessage);
    }

    @Test
    public void testNonDeserializeExceptionForRetryBatchConsumer() {
        Exception exception = new Exception();
        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
        ConsumerRecords<?, ?> data = new ConsumerRecords<>(records);
        messageRejectionService.handleRejectedMessageBatch(exception, data);
        String deserializationErrorMessage =
                "Failed to deserialize message - " + buildMultiMessage(data);
        verify(slackMessagingService, never()).sendDeserializationErrorMessage(deserializationErrorMessage);
    }

    private String buildMultiMessage(ConsumerRecords<?, ?> data) {
        StringBuilder errorMessageDetails = new StringBuilder();
        data.forEach((datum)-> {
            errorMessageDetails.append(
                    buildSingleMessage(datum.topic(), datum.partition(), datum.offset()));
            errorMessageDetails.append("\n");

        });
        return errorMessageDetails.toString();
    }

    private String buildSingleMessage(String topic, int partition, long offset) {
        return "topic: " + topic + ", partition: " + partition + ", offset: " + offset;
    }
}
