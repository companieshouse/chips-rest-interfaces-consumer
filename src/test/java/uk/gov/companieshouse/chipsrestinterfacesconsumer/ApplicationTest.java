package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    @Mock
    private ApplicationLogger logger;

    @Mock
    private LoopingMessageProcessor loopingMainMessageConsumer;

    @Mock
    private LoopingMessageProcessor loopingRetryMessageConsumer;

    @InjectMocks
    private Application application;

    @Test
    void runErrorProcessingTrueTest() {
        ReflectionTestUtils.setField(application, "isErrorConsumer", true);
        application.run();

        verify(loopingMainMessageConsumer, times(0)).loopReadAndProcess();
        verify(loopingRetryMessageConsumer, times(0)).loopReadAndProcess();

        //ToDo Ensure error consumer is called
    }

    @Test
    void runErrorProcessingFalseTest() {
        when(loopingMainMessageConsumer.loopReadAndProcess()).thenReturn(CompletableFuture.completedFuture(true));
        when(loopingRetryMessageConsumer.loopReadAndProcess()).thenReturn(CompletableFuture.completedFuture(true));

        ReflectionTestUtils.setField(application, "isErrorConsumer", false);
        application.run();

        verify(loopingMainMessageConsumer, times(1)).loopReadAndProcess();
        verify(loopingRetryMessageConsumer, times(1)).loopReadAndProcess();

        //ToDo Ensure error consumer is not called
    }
}