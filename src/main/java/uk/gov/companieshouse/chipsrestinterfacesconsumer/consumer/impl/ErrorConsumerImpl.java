package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.ErrorConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.MessageProcessorService;
import uk.gov.companieshouse.service.ServiceException;

@Service
@Profile("error-mode")
public class ErrorConsumerImpl implements ErrorConsumer {

    private final ApplicationLogger logger;
    private final MessageProcessorService messageProcessorService;

    @Autowired
    public ErrorConsumerImpl(ApplicationLogger logger, MessageProcessorService messageProcessorService) {
        this.logger = logger;
        this.messageProcessorService = messageProcessorService;
    }

    @Override
    @KafkaListener(topics = "${kafka.error.topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "error-group")
    public void readAndProcessErrorTopic(@Payload ChipsRestInterfacesSend data,
                                        @Headers MessageHeaders headers){

        logger.info(String.format("received data='%s'", data));

        headers.keySet().forEach(key -> {
            logger.info(String.format("%s: %s", key, headers.get(key)));
        });

        try {
            messageProcessorService.processMessage("Error Consumer", data);
        } catch (ServiceException se) {
            logger.error("Failed to process message", se);
        }
    }
}
