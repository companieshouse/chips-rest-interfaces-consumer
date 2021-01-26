package uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer;

public interface MessageConsumer extends Runnable{
    void readAndProcess();
}
