package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.consumer.MessageConsumer;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static final String APPLICATION_NAME = "chips-rest-interfaces-consumer";

    @Autowired
    @Qualifier("incoming-message-consumer")
    private MessageConsumer incomingMessageConsumer;

    @Autowired
    @Qualifier("retry-message-consumer")
    private MessageConsumer retryMessageConsumer;

    private boolean isRunning = true;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) {
        while(isRunning) {
            incomingMessageConsumer.readAndProcess();
            retryMessageConsumer.readAndProcess();
        }
    }
}
