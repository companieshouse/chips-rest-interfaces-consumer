package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private ApplicationLogger logger;

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
        } else {
            logger.info("***** Application started in error processing mode *****");

        }
    }
}
