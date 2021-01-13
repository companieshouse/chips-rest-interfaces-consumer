package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Service
public class ChipsRestClient {

    private static final String CHIPS_REST_ENDPOINT_URI_VAR = "chips-rest-endpoint";

    @Value("${CHIPS_REST_INTERFACES_HOST}")
    private String chipsRestHost;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApplicationLogger logger;

    private String chipsRestUrl;

    @PostConstruct
    void init() {
        chipsRestUrl = chipsRestHost + "{" + CHIPS_REST_ENDPOINT_URI_VAR + "}";
    }

    public void sendToChips(ChipsRestInterfacesSend message) {
        var messageData = message.getData();
        var restEndpoint = message.getChipsRestEndpoint();

        var uriVariables = Collections.singletonMap(CHIPS_REST_ENDPOINT_URI_VAR, restEndpoint);
        restTemplate.postForEntity(chipsRestUrl, messageData, String.class, uriVariables);
    }
}
