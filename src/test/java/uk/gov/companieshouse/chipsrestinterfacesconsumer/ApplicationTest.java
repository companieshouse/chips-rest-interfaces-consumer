package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ApplicationTest {

    private static final long RETRY_THROTTLE_SECONDS = 1L;
    @Mock
    private ApplicationLogger logger;

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private MessageConsumer incomingMessageConsumer;

    @Mock
    private MessageConsumer retryMessageConsumer;

    @InjectMocks
    private Application application;

    @Test
    void runErrorProcessingTrueTest() {
        ReflectionTestUtils.setField(application, "runAppInErrorMode", true);
        application.run();

        verify(taskScheduler, times(0)).scheduleWithFixedDelay(incomingMessageConsumer, 1L);
        verify(taskScheduler, times(0)).scheduleWithFixedDelay(retryMessageConsumer, 3000L);

        //ToDo Ensure error consumer is called
    }

    @Test
    void runErrorProcessingFalseTest() {
        ReflectionTestUtils.setField(application, "runAppInErrorMode", false);
        ReflectionTestUtils.setField(application, "retryThrottleSeconds", RETRY_THROTTLE_SECONDS);
        application.run();

        verify(taskScheduler, times(1)).scheduleWithFixedDelay(incomingMessageConsumer,1L);
        verify(taskScheduler, times(1)).scheduleWithFixedDelay(retryMessageConsumer, RETRY_THROTTLE_SECONDS * 1000L);

        //ToDo Ensure error consumer is not called
    }
}