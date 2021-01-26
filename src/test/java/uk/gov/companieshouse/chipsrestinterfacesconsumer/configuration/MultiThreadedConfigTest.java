package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiThreadedConfigTest {

    private MultiThreadedConfig multiThreadedConfig;

    @BeforeEach
    void setup() {
        multiThreadedConfig = new MultiThreadedConfig();
    }

    @Test
    void taskExecutorTest() {
        var scheduler = multiThreadedConfig.threadPoolTaskScheduler();

        assertEquals(2, scheduler.getPoolSize());
        assertEquals("CRICMessageProcessor-", scheduler.getThreadNamePrefix());
    }
}