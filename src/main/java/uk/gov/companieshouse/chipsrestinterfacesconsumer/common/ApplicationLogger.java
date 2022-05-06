package uk.gov.companieshouse.chipsrestinterfacesconsumer.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a wrapper for the structured logger to help with unit testing and also ensures that the
 * map data structure passed to the Companies House logger is not changed if used by subsequent
 * logging calls.
 */
@Component
public class ApplicationLogger {

    public static final String KEY_GROUP_ID = "Group Id";
    public static final String KEY_PARTITION = "Partition";
    public static final String KEY_OFFSET = "Offset";
    public static final String KEY_MESSAGE = "Message";
    public static final String KEY_MESSAGE_CONSUMER_ID = "Message Consumer ID";
    public static final String KEY_HTTP_STATUS_CODE = "HTTP Status Code";

    private Logger logger;

    @Value("${RUN_APP_IN_ERROR_MODE:false}")
    private boolean runAppInErrorMode;

    @PostConstruct
    void init() {
        if (runAppInErrorMode) {
            logger = LoggerFactory.getLogger("chips-rest-interfaces-error-consumer");
        } else {
            logger = LoggerFactory.getLogger("chips-rest-interfaces-consumer");
        }
    }

    public void debug(String message) {
        logger.debug(message);
    }

    public void debugContext(String context, String message) {
        logger.debugContext(context, message, null);
    }

    public void debugContext(String context, String message, Map<String, Object> dataMap) {
        logger.debugContext(context, message, cloneMapData(dataMap));
    }

    public void info(String message) {
        logger.info(message, null);
    }

    public void info(String message, Map<String, Object> map) {
        logger.info(message, cloneMapData(map));
    }

    public void infoContext(String context, String message) {
        logger.infoContext(context, message, null);
    }

    public void infoContext(String context, String message, Map<String, Object> dataMap) {
        logger.infoContext(context, message, cloneMapData(dataMap));
    }

    public void errorContext(String context, Exception e) {
        logger.errorContext(context, e, null);
    }

    public void errorContext(String context, Exception e, Map<String, Object> dataMap) {
        logger.errorContext(context, e, cloneMapData(dataMap));
    }

    public void errorContext(String context, String message, Exception e) {
        logger.errorContext(context, message, e, null);
    }

    public void errorContext(String context, String message, Exception e, Map<String, Object> dataMap) {
        logger.errorContext(context, message, e, cloneMapData(dataMap));
    }

    public void error(String message) {
        logger.error(message);
    }

    public void error(String message, Exception e) {
        logger.error(message, e);
    }

    public void error(String message, Exception e, Map<String, Object> dataMap) {
        logger.error(message, e, cloneMapData(dataMap));
    }

    /**
     * The Companies House logging implementation modifies the data map content which means that
     * if the same data map is used for subsequent calls any new message that might be passed in
     * is not displayed in certain log format outputs. Creating a clone of the data map gets around
     * this issue.
     *
     * @param dataMap The map data to log
     * @return A cloned copy of the map data
     */
    private Map<String, Object> cloneMapData(Map<String, Object> dataMap) {
        return new HashMap<>(dataMap);
    }
}
