package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChipsRestClientTest {

    private static final String CHIPS_ENDPOINT = "test-endpoint";
    private static final String DATA = "data";
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChipsRestClient chipsRestClient;

    @Captor
    private ArgumentCaptor<String> messageDataArgCaptor;

    @Captor
    private ArgumentCaptor<Map<String, String>> uriVariablesArgCaptor;

    @Test
    void sendToChipsTest() {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();
        chipsKafkaMessage.setData(DATA);
        chipsKafkaMessage.setChipsRestEndpoint(CHIPS_ENDPOINT);

        chipsRestClient.init();
        chipsRestClient.sendToChips(chipsKafkaMessage);

        verify(restTemplate, times(1)).postForEntity(any(), messageDataArgCaptor.capture(),
                eq(String.class), uriVariablesArgCaptor.capture());

        var messageData = messageDataArgCaptor.getValue();
        var uriVariables = uriVariablesArgCaptor.getValue();

        assertEquals(DATA, messageData);
        assertEquals(CHIPS_ENDPOINT, uriVariables.get("{chips-rest-endpoint}"));
    }
}