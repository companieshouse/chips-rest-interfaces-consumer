package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.impl;

import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay.ConsumerDelayStrategy;

import java.util.HashMap;

public class RetryConsumerDelayStrategy implements ConsumerDelayStrategy {

    private final ApplicationLogger logger;
    private final String consumerId;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleRateSeconds;

    public RetryConsumerDelayStrategy(String consumerId, ApplicationLogger logger) {
        this.consumerId = consumerId;
        this.logger = logger;
    }

    @Override
    public void throttle() {
        try {
            logger.info(String.format("%s Delaying retry consumer for %d seconds", consumerId, retryThrottleRateSeconds));
            long retryThrottleRateMilliseconds = retryThrottleRateSeconds * 1000;
            Thread.sleep(retryThrottleRateMilliseconds);
            logger.info(String.format("%s Delay period over", consumerId));
        } catch(InterruptedException ie) {
            logger.error(String.format("%s interrupted whilst sleeping", Thread.currentThread().getName()), ie);
            Thread.currentThread().interrupt();
        }
    }
}
