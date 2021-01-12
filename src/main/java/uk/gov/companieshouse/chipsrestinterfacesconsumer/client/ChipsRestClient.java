package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service
public class ChipsRestClient {

    @Value("${chips.rest.endpoint}")
    private String chipsRestEndpoint;

    @Value("${CHIPS_REST_INTERFACES_HOST}")
    private String chipsRestHost;

    @Autowired
    private RestTemplate restTemplate;

    private String chipsRestUrl;

    @PostConstruct
    void init() {
        chipsRestUrl = chipsRestHost + chipsRestEndpoint;
    }

    public void sendToChips(ChipsKafkaMessage message) throws RestClientException {
        var messageData = message.getData();
        var restEndpoint = message.getChipsRestEndpoint();

        var uriVariables = Collections.singletonMap(chipsRestEndpoint, restEndpoint);

        restTemplate.postForEntity(chipsRestUrl, messageData, String.class, uriVariables);
    }
}
