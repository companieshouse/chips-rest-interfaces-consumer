package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.throttle;

@FunctionalInterface
public interface ConsumerThrottleStrategy {

    void throttle();
}
