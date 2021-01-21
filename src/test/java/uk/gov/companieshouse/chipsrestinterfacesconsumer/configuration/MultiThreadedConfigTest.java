package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiThreadedConfigTest {

    @Test
    void taskExecutorTest() {
        MultiThreadedConfig multiThreadedConfig = new MultiThreadedConfig();

        var executor = (ThreadPoolTaskExecutor) multiThreadedConfig.taskExecutor();
        assertEquals(2, executor.getMaxPoolSize());
        assertEquals(2, executor.getCorePoolSize());
        assertEquals("CRICMessageProcessor-", executor.getThreadNamePrefix());
    }
}