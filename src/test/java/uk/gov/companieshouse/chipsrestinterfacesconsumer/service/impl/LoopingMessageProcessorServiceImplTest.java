package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoopingMessageProcessorServiceImplTest {

    @Mock
    private ApplicationLogger logger;

    @Mock
    private MessageConsumer messageConsumer;

    @InjectMocks
    private LoopingMessageProcessorServiceImpl loopingMessageProcessorService;

    @Test
    void loopReadAndProcess() {
        doAnswer(invocation -> {
            ReflectionTestUtils.setField(loopingMessageProcessorService, "isRunning", false);
            return null;
        }).when(messageConsumer).readAndProcess();

        var loopingMessageProcessorServiceResult =
                loopingMessageProcessorService.loopReadAndProcess();

        verify(messageConsumer, times(1)).readAndProcess();
    }
}