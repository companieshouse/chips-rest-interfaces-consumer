package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.ConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.impl.RetryConsumerThrottleStrategy;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl.LoopingMessageProcessorServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultiThreadedConfigTest {

    private static final String LOGGER_FIELD = "logger";
    private static final String CONSUMER_FIELD = "consumer";
    private static final String THROTTLE_STRATEGY_FIELD = "throttleStrategy";
    private static final String ID_FIELD = "id";

    @Mock
    private ApplicationLogger applicationLogger;

    @Mock
    private MessageConsumer messageConsumer;

    @Mock
    private ConsumerThrottleStrategy consumerThrottleStrategy;

    private MultiThreadedConfig multiThreadedConfig;

    @BeforeEach
    void setup() {
        multiThreadedConfig = new MultiThreadedConfig();
    }

    @Test
    void mainLoopingConsumerTest() {
        LoopingMessageProcessor loopingMessageProcessor = multiThreadedConfig.mainLoopingConsumer(applicationLogger, messageConsumer);

        assertTrue(loopingMessageProcessor instanceof LoopingMessageProcessorServiceImpl);
        assertEquals(applicationLogger, ReflectionTestUtils.getField(loopingMessageProcessor, LOGGER_FIELD));
        assertEquals(messageConsumer, ReflectionTestUtils.getField(loopingMessageProcessor, CONSUMER_FIELD));
        assertEquals(consumerThrottleStrategy, ReflectionTestUtils.getField(loopingMessageProcessor, THROTTLE_STRATEGY_FIELD));
        assertEquals("main-looping-consumer", ReflectionTestUtils.getField(loopingMessageProcessor, ID_FIELD));
    }

    @Test
    void retryLoopingConsumerTest() {
        LoopingMessageProcessor loopingMessageProcessor = multiThreadedConfig.retryLoopingConsumer(applicationLogger, messageConsumer, consumerThrottleStrategy);

        assertTrue(loopingMessageProcessor instanceof LoopingMessageProcessorServiceImpl);
        assertEquals(applicationLogger, ReflectionTestUtils.getField(loopingMessageProcessor, LOGGER_FIELD));
        assertEquals(messageConsumer, ReflectionTestUtils.getField(loopingMessageProcessor, CONSUMER_FIELD));
        assertEquals(consumerThrottleStrategy, ReflectionTestUtils.getField(loopingMessageProcessor, THROTTLE_STRATEGY_FIELD));
        assertEquals("retry-looping-consumer", ReflectionTestUtils.getField(loopingMessageProcessor, ID_FIELD));
    }

    @Test
    void retryConsumerThrottleStrategyTest() {
        ConsumerThrottleStrategy throttleStrategy = multiThreadedConfig.retryConsumerThrottleStrategy(applicationLogger);

        assertTrue(throttleStrategy instanceof RetryConsumerThrottleStrategy);
        var consumerId = ReflectionTestUtils.getField(throttleStrategy, "consumerId");
        assertEquals("retry-looping-consumer", consumerId);
    }

    @Test
    void taskExecutorTest() {
        var executor = (ThreadPoolTaskExecutor) multiThreadedConfig.taskExecutor();

        assertEquals(2, executor.getMaxPoolSize());
        assertEquals(2, executor.getCorePoolSize());
        assertEquals("CRICMessageProcessor-", executor.getThreadNamePrefix());
    }
}