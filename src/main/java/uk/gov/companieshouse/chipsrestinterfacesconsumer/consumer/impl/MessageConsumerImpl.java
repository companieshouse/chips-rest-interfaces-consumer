package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.kafka.consumer.resilience.CHKafkaResilientConsumerGroup;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.exceptions.DeserializationException;
import uk.gov.companieshouse.kafka.message.Message;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageConsumerImpl implements MessageConsumer {

    private final ApplicationLogger logger;

    private final MessageProcessorService messageProcessorService;

    private final DeserializerFactory deserializerFactory;

    private final CHKafkaResilientConsumerGroup consumer;

    private final String id;

    private final Object lock = new Object();

    private AtomicBoolean processing = new AtomicBoolean(false);

    public MessageConsumerImpl(ApplicationLogger logger,
                               MessageProcessorService messageProcessorService,
                               DeserializerFactory deserializerFactory,
                               CHKafkaResilientConsumerGroup consumer,
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
        synchronized(lock) {
            try {
                while(processing.get()) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            consumer.close();
            if(!processing.get()) {
                lock.notify();
            }
        }
    }

    @Override
    public void run() {
        this.readAndProcess();
    }

    @Override
    public void readAndProcess() {
        processing.set(true);
        for (Message msg : consumer.consume()) {
            try {
                logger.info(String.format("%s - Message offset %s, partition %s retrieved, processing", id, msg.getOffset(), msg.getPartition()));

                ChipsRestInterfacesSend deserializedMsg = deserialize(msg);

                Map<String, Object> logMap = new HashMap<>();
                logMap.put("Message Offset", msg.getOffset());
                logMap.put("Deserialised message id", deserializedMsg.getMessageId());
                logMap.put("Thread Name", Thread.currentThread().getName());
                logMap.put("Message Consumer ID", id);
                var messageId = deserializedMsg.getMessageId();
                logger.infoContext(messageId, String.format("%s - Message deserialised successfully", id), logMap);

                messageProcessorService.processMessage(id, deserializedMsg);

                logger.infoContext(messageId, String.format("%s - Message offset %s, partition %s processed, committing offset", id, msg.getOffset(), msg.getPartition()));

                consumer.commit(msg);
            } catch (Exception e) {
                Map<String, Object> data = new HashMap<>();
                data.put("message", msg.getValue() == null ? "" : new String(msg.getValue()));
                logger.error(String.format("%s - Failed to read message from queue", id), e, data);
            } finally {
                processing.set(false);
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
