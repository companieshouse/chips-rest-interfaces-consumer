package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl.LoopingMessageProcessorServiceImpl;

import java.util.concurrent.Executor;

@Configuration
public class MultiThreadedConfig {

    @Bean("main-looping-consumer")
    LoopingMessageProcessor mainLoopingConsumer(ApplicationLogger logger,
                                                @Qualifier("incoming-message-consumer") MessageConsumer messageConsumer
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, logger, "main-looping-consumer");
    }

    @Bean("retry-looping-consumer")
    LoopingMessageProcessor retryLoopingConsumer(ApplicationLogger logger,
                                                 @Qualifier("retry-message-consumer") MessageConsumer messageConsumer
    ) {
        return new LoopingMessageProcessorServiceImpl(messageConsumer, logger, "retry-looping-consumer");
    }

    @Bean
    Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("KafkaConsumer-");
        executor.initialize();
        return executor;
    }
}
