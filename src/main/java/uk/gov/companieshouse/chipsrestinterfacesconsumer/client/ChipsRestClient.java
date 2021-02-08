package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
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

    private UriTemplate chipsRestUrl;

    @PostConstruct
    void init() {
        chipsRestUrl = new UriTemplate(String.format("%s{%s}", chipsRestHost, CHIPS_REST_ENDPOINT_URI_VAR));
    }

    public void sendToChips(ChipsRestInterfacesSend message) {
        var messageData = message.getData();
        var restEndpoint = message.getChipsRestEndpoint();

        var uriVariables = Collections.singletonMap(CHIPS_REST_ENDPOINT_URI_VAR, restEndpoint);

        HttpHeaders requestHeaders = new HttpHeaders();
        //Chips rest requires content type to be json
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

        var expandedUrl = chipsRestUrl.expand(uriVariables);
        var messageId = message.getMessageId();
        logger.infoContext(messageId , String.format("Posting this message to %s", expandedUrl));
        HttpEntity<String> requestEntity = new HttpEntity<>(messageData, requestHeaders);
        var response = restTemplate.exchange(
                expandedUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        logger.infoContext(messageId, String.format(
                "Message successfully sent, Chips Rest Response Status: %s", response.getStatusCode()));
    }
}
