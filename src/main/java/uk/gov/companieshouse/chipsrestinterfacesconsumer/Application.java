package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
public class Application implements CommandLineRunner {

    public static final String APPLICATION_NAME = "chips-rest-interfaces-consumer";

    @Autowired
    @Lazy
    @Qualifier("main-looping-consumer")
    private LoopingMessageProcessor loopingMainMessageConsumer;

    @Autowired
    @Lazy
    @Qualifier("retry-looping-consumer")
    private LoopingMessageProcessor loopingRetryMessageConsumer;

    @Autowired
    private ApplicationLogger logger;

    @Value("${RUN_APP_IN_ERROR_MODE:false}")
    private boolean isErrorConsumer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) {
        if (!isErrorConsumer) {
            logger.info(String.format("%s started in normal processing mode", APPLICATION_NAME));
            var mainCompletableFuture = loopingMainMessageConsumer.loopReadAndProcess();
            var retryCompletableFuture = loopingRetryMessageConsumer.loopReadAndProcess();

            // Wait until they are all done
            CompletableFuture.allOf(mainCompletableFuture, retryCompletableFuture).join();
        } else {
            logger.info(String.format("%s started in error processing mode", APPLICATION_NAME));

            //ToDo Start error consumer
        }
    }
}
