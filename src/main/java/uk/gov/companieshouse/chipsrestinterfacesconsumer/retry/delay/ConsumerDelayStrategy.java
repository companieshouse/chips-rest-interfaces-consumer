package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.delay;

import uk.gov.companieshouse.service.ServiceException;

@FunctionalInterface
public interface ConsumerDelayStrategy {

    void throttle();
}
