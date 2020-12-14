package uk.gov.companieshouse.chipsrestinterfacesconsumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.processor.MessageConsumer;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private MessageConsumer incomingMessageConsumer;

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Override
    public void run(String... args) throws Exception {
        while(true) {
            incomingMessageConsumer.read();
        }
    }
}
