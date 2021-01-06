package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;

@Service
public class ChipsRestService {

    @Value("${CHIPS_REST_INTERFACES_HOST}")
    private String chipsRestHost;

    @Autowired
    private RestTemplate restTemplate;

    private String chipsRestUrl;

    @PostConstruct
    void init() {
        chipsRestUrl = chipsRestHost + "{chips-rest-endpoint}";
    }

    void sendToChips(Collection<ChipsKafkaMessage> messages) {
        messages.parallelStream().forEach((message -> {
            var messageData = message.getData();
            var restEndpoint = message.getChipsRestEndpoint();

            var uriVariables = Collections.singletonMap("chips-rest-endpoint", restEndpoint);

            restTemplate.postForEntity(chipsRestUrl, messageData, String.class, uriVariables);
        }));
    }
}
