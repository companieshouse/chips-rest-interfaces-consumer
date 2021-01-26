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

    public static final String APPLICATION_NAME = "chips-rest-interfaces-consumer";

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
            logger.info(String.format("%s started in normal processing mode", APPLICATION_NAME));
            var retryThrottleMillis = retryThrottleSeconds * 1000L;
            taskScheduler.scheduleWithFixedDelay(mainMessageConsumer, 1L);
            taskScheduler.scheduleWithFixedDelay(retryMessageConsumer, retryThrottleMillis);
        } else {
            logger.info(String.format("%s started in error processing mode", APPLICATION_NAME));

            //ToDo Start error consumer
        }
    }
}
