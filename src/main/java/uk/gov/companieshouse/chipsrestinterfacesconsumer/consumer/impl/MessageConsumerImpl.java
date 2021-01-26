package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

public class MessageConsumerImpl implements MessageConsumer {

    private final ApplicationLogger logger;

    private final MessageProcessorService messageProcessorService;

    private final DeserializerFactory deserializerFactory;

    private final CHKafkaConsumerGroup consumer;

    private final String id;

    public MessageConsumerImpl(ApplicationLogger logger,
                               MessageProcessorService messageProcessorService,
                               DeserializerFactory deserializerFactory,
                               CHKafkaConsumerGroup consumer,
                               String id) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
        this.deserializerFactory = deserializerFactory;
        this.consumer = consumer;
        this.id = id;
    }

    @PostConstruct
    void init() {
        logger.info(id + " initialised");
        consumer.connect();
    }

    @PreDestroy
    void close() {
        consumer.close();
    }

    @Override
    public void run() {
        this.readAndProcess();
    }

    @Override
    public void readAndProcess() {
        for (Message msg : consumer.consume()) {
            try {
                logger.info(String.format("%s - Message offset %s retrieved, processing", id, msg.getOffset()));

                ChipsRestInterfacesSend deserializedMsg = deserialize(msg);

                Map<String, Object> logMap = new HashMap<>();
                logMap.put("Message Offset", msg.getOffset());
                logMap.put("Deserialised message id", deserializedMsg.getMessageId());
                logMap.put("Thread Name", Thread.currentThread().getName());
                logMap.put("Message Consumer ID", id);
                logger.info(String.format("%s - Message deserialised successfully", id), logMap);

                messageProcessorService.processMessage(deserializedMsg);

                logger.info(String.format("%s - Message offset %s processed, committing offset", id, msg.getOffset()));

                consumer.commit(msg);
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", msg.getValue() == null ? "" : new String(msg.getValue()));
                logger.error(String.format("%s - Failed to read message from queue", id), e, data);
            }
        }
    }

    private ChipsRestInterfacesSend deserialize(Message msg) throws DeserializationException {
        return deserializerFactory
                .getSpecificRecordDeserializer(ChipsRestInterfacesSend.class)
                .fromBinary(msg, ChipsRestInterfacesSend.getClassSchema());
    }

    public String getId() {
        return id;
    }
}
