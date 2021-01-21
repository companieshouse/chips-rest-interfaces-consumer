package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.service.LoopingMessageProcessor;

import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@EnableAsync
public class Application implements CommandLineRunner {

    public static final String APPLICATION_NAME = "chips-rest-interfaces-consumer";

    @Autowired
    @Qualifier("main-looping-consumer")
    private LoopingMessageProcessor loopingMainMessageConsumer;

    @Autowired
    @Qualifier("retry-looping-consumer")
    private LoopingMessageProcessor loopingRetryMessageConsumer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) {
        var mainCompletableFuture = loopingMainMessageConsumer.loopReadAndProcess();
        var retryCompletableFuture = loopingRetryMessageConsumer.loopReadAndProcess();

        // Wait until they are all done
        CompletableFuture.allOf(mainCompletableFuture, retryCompletableFuture).join();
    }
}
