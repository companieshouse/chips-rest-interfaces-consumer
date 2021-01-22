package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.ConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.impl.MainConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.impl.RetryConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl.LoopingMessageProcessorServiceImpl;

import java.util.concurrent.Executor;

@Configuration
class MultiThreadedConfig {

    @Bean("main-looping-consumer")
    LoopingMessageProcessor mainLoopingConsumer(ApplicationLogger logger,
                                                @Qualifier("incoming-message-consumer") MessageConsumer messageConsumer,
                                                @Qualifier("main-consumer-throttle-strategy") ConsumerThrottleStrategy throttleStrategy
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, throttleStrategy, logger, "main-looping-consumer");
    }

    @Bean("retry-looping-consumer")
    LoopingMessageProcessor retryLoopingConsumer(ApplicationLogger logger,
                                                 @Qualifier("retry-message-consumer") MessageConsumer messageConsumer,
                                                 @Qualifier("retry-consumer-throttle-strategy") ConsumerThrottleStrategy throttleStrategy
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, throttleStrategy, logger, "retry-looping-consumer");
    }

    @Bean("main-consumer-throttle-strategy")
    ConsumerThrottleStrategy mainConsumerThrottleStrategy(){
        return new MainConsumerThrottleStrategy();
    }

    @Bean("retry-consumer-throttle-strategy")
    ConsumerThrottleStrategy mainConsumerThrottleStrategy(ApplicationLogger logger){
        return new RetryConsumerThrottleStrategy("retry-consumer-throttle-strategy", logger);
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
