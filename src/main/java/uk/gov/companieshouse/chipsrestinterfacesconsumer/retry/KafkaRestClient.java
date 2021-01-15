package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class KafkaRestClient {
    private final RestTemplate restTemplate;

    public KafkaRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public byte[] getSchema(String schemaRegistryUrl, String schemaUri) {
        String schemaUrl = String.format("%s%s", schemaRegistryUrl, schemaUri);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(schemaUrl, HttpMethod.GET, entity, byte[].class);
        return response.getBody();
    }
}
