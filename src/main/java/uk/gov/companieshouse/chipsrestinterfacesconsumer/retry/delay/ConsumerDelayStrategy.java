package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay;

@FunctionalInterface
public interface ConsumerDelayStrategy {

    void throttle();
}
