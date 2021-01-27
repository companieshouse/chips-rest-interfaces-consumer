package uk.gov.companieshouse.chipsrestinterfacesconsumer.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationLoggerTest {

    private static final String CONTEXT = "CONTEXT";
    private static final String TEST_MESSAGE = "TEST";
    private static final String LOG_MAP_KEY = "COMPANY_NUMBER";
    private static final String LOG_MAP_VALUE = "00006400";

    private ApplicationLogger applicationLogger;

    private Map<String, Object> logMap;

    @BeforeEach
    void setup() {
        applicationLogger = new ApplicationLogger();
        applicationLogger.init();
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
    void testErrorNoContextNoMap() {
        applicationLogger.error(TEST_MESSAGE, new Exception(TEST_MESSAGE));

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
        assertDoesNotThrow(() -> applicationLogger.debugContext(CONTEXT, TEST_MESSAGE));
    }

    @Test
    void testInfo() {
        assertDoesNotThrow(() -> applicationLogger.info(TEST_MESSAGE));
    }

    @Test
    void testInfoContext() {
        assertDoesNotThrow(() -> applicationLogger.infoContext(CONTEXT, TEST_MESSAGE));
    }

    @Test
    void testErrorContextNoMessage() {
        assertDoesNotThrow(() -> applicationLogger.errorContext(CONTEXT, new Exception()));
    }

    @Test
    void testErrorContext() {
        assertDoesNotThrow(() -> applicationLogger.errorContext(CONTEXT, TEST_MESSAGE, new Exception()));
    }
}
