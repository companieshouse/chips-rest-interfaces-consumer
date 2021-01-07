package uk.gov.companieshouse.chipsrestinterfacesconsumer.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ApplicationLoggerTest {

    private static final String CONTEXT = "CONTEXT";
    private static final String TEST_MESSAGE = "TEST";
    private static final String LOG_MAP_KEY = "COMPANY_NUMBER";
    private static final String LOG_MAP_VALUE = "00006400";

    @InjectMocks
    private static ApplicationLogger applicationLogger;

    private Map<String, Object> logMap;

    @BeforeEach
    void setup() {
        logMap = new HashMap<>();
        logMap.put(LOG_MAP_KEY, LOG_MAP_VALUE);
    }

    @Test
    void testDebugContextLoggingDoesNotModifyLogMap() {
        applicationLogger.debugContext(CONTEXT, TEST_MESSAGE, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testInfoContextLoggingDoesNotModifyLogMap() {
        applicationLogger.infoContext(CONTEXT, TEST_MESSAGE, logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorContextLoggingDoesNotModifyLogMap() {
        applicationLogger.errorContext(CONTEXT, TEST_MESSAGE, new Exception(TEST_MESSAGE), logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testErrorNoContextLoggingDoesNotModifyLogMap() {
        applicationLogger.error(TEST_MESSAGE, new Exception(TEST_MESSAGE), logMap);

        assertEquals(1, logMap.size());
        assertEquals(LOG_MAP_VALUE, logMap.get(LOG_MAP_KEY));
    }

    @Test
    void testDebugContext() {
        applicationLogger.debugContext(CONTEXT, TEST_MESSAGE);
    }

    @Test
    void testInfo() {
        applicationLogger.info(TEST_MESSAGE);
    }

    @Test
    void testInfoContext() {
        applicationLogger.infoContext(CONTEXT, TEST_MESSAGE);
    }

    @Test
    void testErrorContextNoMessage() {
        applicationLogger.errorContext(CONTEXT, new Exception());
    }

    @Test
    void testErrorContext() {
        applicationLogger.errorContext(CONTEXT, TEST_MESSAGE, new Exception());
    }
}
