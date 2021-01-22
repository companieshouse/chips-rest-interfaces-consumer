package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.ConsumerDelayStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.impl.MainConsumerDelayStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.impl.RetryConsumerDelayStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl.LoopingMessageProcessorServiceImpl;

import java.util.concurrent.Executor;

@Configuration
class MultiThreadedConfig {

    @Bean("main-looping-consumer")
    LoopingMessageProcessor mainLoopingConsumer(ApplicationLogger logger,
                                                @Qualifier("incoming-message-consumer") MessageConsumer messageConsumer,
                                                @Qualifier("main-consumer-delay-strategy") ConsumerDelayStrategy delayStrategy
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, delayStrategy, logger, "main-looping-consumer");
    }

    @Bean("retry-looping-consumer")
    LoopingMessageProcessor retryLoopingConsumer(ApplicationLogger logger,
                                                 @Qualifier("retry-message-consumer") MessageConsumer messageConsumer,
                                                 @Qualifier("retry-consumer-delay-strategy") ConsumerDelayStrategy delayStrategy
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, delayStrategy, logger, "retry-looping-consumer");
    }

    @Bean("main-consumer-delay-strategy")
    ConsumerDelayStrategy mainConsumerDelayStrategy(){
        return new MainConsumerDelayStrategy();
    }

    @Bean("retry-consumer-delay-strategy")
    ConsumerDelayStrategy mainConsumerDelayStrategy(ApplicationLogger logger){
        return new RetryConsumerDelayStrategy("retry-consumer-delay-strategy", logger);
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
