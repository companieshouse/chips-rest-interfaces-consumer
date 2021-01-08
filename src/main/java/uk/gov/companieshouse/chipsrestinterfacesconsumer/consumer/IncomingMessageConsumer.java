package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHConsumer;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class IncomingMessageConsumer implements MessageConsumer {

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private CHKafkaConsumerGroup consumer;

    @Autowired
    private MessageProcessorService messageProcessorService;

    @PostConstruct
    void init() {
        consumer.connect();
    }

    @PreDestroy
    void close() {
        consumer.close();
    }

    @Override
    public void readAndProcess() {
        for (Message msg : consumer.consume()) {
            try {
                logger.info(msg.toString());
                ChipsKafkaMessage deserializedMsg = deserialize(msg);
                messageProcessorService.processMessage(deserializedMsg);
                consumer.commit(msg);
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", msg.getValue() == null ? "" : new String(msg.getValue()));
                logger.error("Failed to read message from queue", e, data);
            }
        }
    }

    private ChipsKafkaMessage deserialize(Message msg) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(msg.getValue(), ChipsKafkaMessage.class);
    }
}
