package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import java.util.concurrent.CompletableFuture;

public interface LoopingMessageProcessor {
    CompletableFuture<Boolean> loopReadAndProcess();
}
