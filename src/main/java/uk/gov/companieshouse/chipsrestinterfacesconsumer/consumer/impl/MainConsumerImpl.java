package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MainConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

@Service
public class MainConsumerImpl implements MainConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    @Autowired
    public MainConsumerImpl(ApplicationLogger logger, MessageProcessorService messageProcessorService) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
    }

    @Override
    @KafkaListener(topics = "${kafka.main.topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "main-group")
    public void readAndProcessMainTopic(@Payload ChipsRestInterfacesSend data,
                                        @Headers MessageHeaders headers){

        logger.info(String.format("received data='%s'", data));

        headers.keySet().forEach(key -> {
            logger.info(String.format("%s: %s", key, headers.get(key)));
        });

        try {
            messageProcessorService.processMessage("Main Consumer", data);
        } catch (ServiceException se) {
            logger.error("Failed to process message", se);
        }
    }

    @Override
    @KafkaListener(topics = "${kafka.retry.topic}", containerFactory = "kafkaRetryListenerContainerFactory", groupId = "retry-group")
    public void readAndProcessRetryTopic(@Payload ChipsRestInterfacesSend data,
                                        @Headers MessageHeaders headers){

        logger.info(String.format("received data='%s'", data));

        headers.keySet().forEach(key -> {
            logger.info(String.format("%s: %s", key, headers.get(key)));
        });

        try {
            messageProcessorService.processMessage("Retry Consumer", data);
        } catch (ServiceException se) {
            logger.error("Failed to process message", se);
        }
    }
}
