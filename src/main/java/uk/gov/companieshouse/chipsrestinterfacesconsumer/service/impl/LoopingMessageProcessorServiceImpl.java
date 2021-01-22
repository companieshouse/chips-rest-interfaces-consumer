package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.springframework.scheduling.annotation.Async;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.ConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;

import javax.annotation.PreDestroy;
import java.util.concurrent.CompletableFuture;

public class LoopingMessageProcessorServiceImpl implements LoopingMessageProcessor {

    private final MessageConsumer consumer;
    private final ConsumerThrottleStrategy throttleStrategy;
    private final ApplicationLogger logger;
    private final String id;
    private boolean isRunning = true;

    public LoopingMessageProcessorServiceImpl(MessageConsumer consumer,
                                              ConsumerThrottleStrategy throttleStrategy,
                                              ApplicationLogger logger,
                                              String id) {
        this.consumer = consumer;
        this.throttleStrategy = throttleStrategy;
        this.logger = logger;
        this.id = id;
    }

    @PreDestroy
    void preDestroy() {
        isRunning = false;
    }

    @Async
    @Override
    public CompletableFuture<Boolean> loopReadAndProcess() {
        logger.info(String.format("%s - Read and process loop starting", id));
        while (isRunning) {
            this.consumer.readAndProcess();
            throttleStrategy.throttle();
        }

        logger.info(String.format("%s - Read and process loop ended", id));

        return CompletableFuture.completedFuture(true);
    }
}
