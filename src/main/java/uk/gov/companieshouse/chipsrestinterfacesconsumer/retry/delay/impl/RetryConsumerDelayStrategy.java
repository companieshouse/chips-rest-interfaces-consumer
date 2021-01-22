package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.impl;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.ConsumerDelayStrategy;

public class RetryConsumerDelayStrategy implements ConsumerDelayStrategy {

    private final ApplicationLogger logger;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleRateSeconds;

    public RetryConsumerDelayStrategy(ApplicationLogger logger) {
        this.logger = logger;
    }

    @Override
    public void throttle() {
        try {
            logger.info("Delaying retry consumer for " + retryThrottleRateSeconds + " seconds");
            long retryThrottleRateMilliseconds = retryThrottleRateSeconds * 1000;
            Thread.sleep(retryThrottleRateMilliseconds);
            logger.info("Delay period over");
        } catch(InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
