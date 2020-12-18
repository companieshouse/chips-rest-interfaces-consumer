package uk.gov.companieshouse.chipsrestinterfacesconsumer.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.Application;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.consumer.CHConsumer;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(Application.APPLICATION_NAME);

    @Autowired
    private CHConsumer consumer;

    @PostConstruct
    void init() {
        consumer.connect();
    }

    @PreDestroy
    void close() {
        consumer.close();
    }

    @Override
    public Collection<ChipsKafkaMessage> read() {
        List<ChipsKafkaMessage> receivedList = new ArrayList<>();
        for (Message msg : consumer.consume()) {
            try {
                logger.info(msg.toString());
                receivedList.add(deserialise(msg));
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", msg.getValue() == null ? "" : new String(msg.getValue()));
                logger.error("Failed to read message from queue", e, data);
            }
        }
        return receivedList;
    }

    ChipsKafkaMessage deserialise(Message msg) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(msg.getValue(), ChipsKafkaMessage.class);
    }
}
