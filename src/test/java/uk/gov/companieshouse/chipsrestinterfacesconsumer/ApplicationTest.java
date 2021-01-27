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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
    private MessageConsumer mainMessageConsumer;

    @Mock
    private MessageConsumer retryMessageConsumer;

    @Mock
    private MessageConsumer errorMessageConsumer;

    @InjectMocks
    private Application application;

    @Test
    void runErrorProcessingTrueTest() {
        ReflectionTestUtils.setField(application, "runAppInErrorMode", true);
        application.run();

        verify(taskScheduler, times(0)).scheduleWithFixedDelay(eq(mainMessageConsumer), anyLong());
        verify(taskScheduler, times(0)).scheduleWithFixedDelay(eq(retryMessageConsumer), anyLong());
        verify(taskScheduler, times(1)).scheduleWithFixedDelay(errorMessageConsumer, 1L);
    }

    @Test
    void runErrorProcessingFalseTest() {
        ReflectionTestUtils.setField(application, "runAppInErrorMode", false);
        ReflectionTestUtils.setField(application, "retryThrottleSeconds", RETRY_THROTTLE_SECONDS);
        application.run();

        verify(taskScheduler, times(1)).scheduleWithFixedDelay(mainMessageConsumer,1L);
        verify(taskScheduler, times(1)).scheduleWithFixedDelay(retryMessageConsumer, RETRY_THROTTLE_SECONDS * 1000L);
        verify(taskScheduler, times(0)).scheduleWithFixedDelay(eq(errorMessageConsumer), anyLong());
    }
}