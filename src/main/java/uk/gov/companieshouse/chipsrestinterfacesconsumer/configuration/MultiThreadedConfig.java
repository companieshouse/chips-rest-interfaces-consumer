package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.ConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.impl.RetryConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl.LoopingMessageProcessorServiceImpl;

import java.util.concurrent.Executor;

@Configuration
class MultiThreadedConfig {

    private static final String MAIN_CONSUMER_ID = "main-looping-consumer";
    private static final String RETRY_CONSUMER_ID = "retry-looping-consumer";

    @Bean("main-looping-consumer")
    LoopingMessageProcessor mainLoopingConsumer(ApplicationLogger logger,
                                                @Qualifier("incoming-message-consumer") MessageConsumer messageConsumer
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, null, logger, MAIN_CONSUMER_ID);
    }

    @Bean("retry-looping-consumer")
    LoopingMessageProcessor retryLoopingConsumer(ApplicationLogger logger,
                                                 @Qualifier("retry-message-consumer") MessageConsumer messageConsumer,
                                                 @Qualifier("retry-consumer-throttle-strategy") ConsumerThrottleStrategy throttleStrategy
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, throttleStrategy, logger, RETRY_CONSUMER_ID);
    }

    @Bean("retry-consumer-throttle-strategy")
    ConsumerThrottleStrategy retryConsumerThrottleStrategy(ApplicationLogger logger) {
        return new RetryConsumerThrottleStrategy(RETRY_CONSUMER_ID, logger);
    }

    @Bean
    Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("CRICMessageProcessor-");
        executor.initialize();
        return executor;
    }
}
