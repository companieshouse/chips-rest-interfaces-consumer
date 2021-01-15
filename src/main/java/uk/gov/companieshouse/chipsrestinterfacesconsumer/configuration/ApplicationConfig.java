package uk.gov.companieshouse.chipsrestinterfacesconsumer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.retry.KafkaRestClient;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public KafkaRestClient restClient(RestTemplate restTemplate) {
        return new KafkaRestClient(restTemplate);
    }
}
