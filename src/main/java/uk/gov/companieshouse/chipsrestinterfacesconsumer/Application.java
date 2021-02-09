package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private ApplicationLogger logger;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    @Lazy
    @Qualifier("main-message-consumer")
    private MessageConsumer mainMessageConsumer;

    @Autowired
    @Lazy
    @Qualifier("retry-message-consumer")
    private MessageConsumer retryMessageConsumer;

    @Autowired
    @Lazy
    @Qualifier("error-message-consumer")
    private MessageConsumer errorMessageConsumer;

    @Value("${RUN_APP_IN_ERROR_MODE:false}")
    private boolean runAppInErrorMode;

    @Value("${RETRY_THROTTLE_RATE_SECONDS}")
    private long retryThrottleSeconds;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) {
        if (!runAppInErrorMode) {
            logger.info("***** Application started in normal processing mode *****");
            var retryThrottleMillis = retryThrottleSeconds * 1000L;

            mainMessageConsumer.readAndProcess();
            retryMessageConsumer.readAndProcess();

            taskScheduler.scheduleWithFixedDelay(mainMessageConsumer, 1L);
            taskScheduler.scheduleWithFixedDelay(retryMessageConsumer, retryThrottleMillis);
        } else {
            logger.info("***** Application started in error processing mode *****");

            try {
                errorMessageConsumer.readAndProcess();
            } catch (Exception e) {
                logger.error(String.format("Error consuming error topic: %s", e.getMessage()), e);
            }
        }
    }
}
