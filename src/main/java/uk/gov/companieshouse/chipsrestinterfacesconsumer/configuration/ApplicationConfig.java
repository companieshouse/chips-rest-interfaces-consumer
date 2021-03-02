package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.function.Supplier;

@Configuration
class ApplicationConfig {

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public Supplier<Long> timestampNow() {
        return () -> Instant.now().toEpochMilli();
    }
}
