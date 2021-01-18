package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Component("incoming-message-consumer")
public class IncomingMessageConsumer implements MessageConsumer {

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    @Qualifier("incoming-consumer-group")
    private CHKafkaConsumerGroup consumer;

    @Autowired
    private MessageProcessorService messageProcessorService;

    @Autowired
    private DeserializerFactory deserializerFactory;

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
                logger.info(String.format("Message offset %s retrieved, processing", msg.getOffset()));
                ChipsRestInterfacesSend deserializedMsg = deserialize(msg);
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("Message Offset", msg.getOffset());
                logMap.put("Deserialised message id", deserializedMsg.getMessageId());
                logger.info("Message deserialised successfully", logMap);
                messageProcessorService.processMessage(deserializedMsg);
                logger.info(String.format("Message offset %s processed, committing offset", msg.getOffset()));
                consumer.commit(msg);
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", msg.getValue() == null ? "" : new String(msg.getValue()));
                logger.error("Failed to read message from queue", e, data);
            }
        }
    }

    private ChipsRestInterfacesSend deserialize(Message msg) throws DeserializationException {
        return deserializerFactory.getSpecificRecordDeserializer(ChipsRestInterfacesSend.class).fromBinary(msg,
                ChipsRestInterfacesSend.getClassSchema());
    }
}
