package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;

@Configuration
public class KafkaConfiguration {

    @Bean
    DeserializerFactory deserializerFactory() {
        return new DeserializerFactory();
    }
}
