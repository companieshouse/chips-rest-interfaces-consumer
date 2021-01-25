package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RetryConsumerThrottleStrategyTest {

    @Mock
    private ApplicationLogger logger;

    @Test
    void testThreadSleepsForSpecifiedForTwoSecondDelay() {
        int twoSeconds = 2;
        RetryConsumerThrottleStrategy retryConsumerThrottleStrategy = new RetryConsumerThrottleStrategy("test", logger);
        ReflectionTestUtils.setField(retryConsumerThrottleStrategy, "retryThrottleRateSeconds", twoSeconds);
        Instant start = Instant.now();
        retryConsumerThrottleStrategy.throttle();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        assertEquals(twoSeconds, timeElapsed/1000);
    }

    @Test
    void testThreadSleepsForSpecifiedForFiveSecondDelay() {
        int fiveSeconds = 5;
        RetryConsumerThrottleStrategy retryConsumerThrottleStrategy = new RetryConsumerThrottleStrategy("test", logger);
        ReflectionTestUtils.setField(retryConsumerThrottleStrategy, "retryThrottleRateSeconds", fiveSeconds);
        Instant start = Instant.now();
        retryConsumerThrottleStrategy.throttle();
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        assertEquals(fiveSeconds, timeElapsed/1000);
    }
}
