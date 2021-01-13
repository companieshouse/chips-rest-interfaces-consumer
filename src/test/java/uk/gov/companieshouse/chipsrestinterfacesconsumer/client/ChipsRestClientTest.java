package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChipsRestClientTest {

    private static final String CHIPS_REST_HOST = "hostname/";
    private static final String CHIPS_REST_ENDPOINT = "test-endpoint";
    private static final String DATA = "data";
    private static final String CHIPS_REST_ENDPOINT_URI_VAR_PLACEHOLDER = "{chips-rest-endpoint}";
    private static final String CHIPS_REST_ENDPOINT_URI_VAR = "chips-rest-endpoint";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private ChipsRestClient chipsRestClient;

    @Captor
    private ArgumentCaptor<String> messageDataArgCaptor;

    @Captor
    private ArgumentCaptor<Map<String, String>> uriVariablesArgCaptor;

    @Test
    void sendToChipsTest() {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DATA);
        chipsRestInterfacesSend.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        ReflectionTestUtils.setField(chipsRestClient, "chipsRestHost", CHIPS_REST_HOST);
        chipsRestClient.init();
        chipsRestClient.sendToChips(chipsRestInterfacesSend);

        verify(restTemplate, times(1)).postForEntity(
                eq(CHIPS_REST_HOST + CHIPS_REST_ENDPOINT_URI_VAR_PLACEHOLDER), messageDataArgCaptor.capture(),
                eq(String.class), uriVariablesArgCaptor.capture());

        var messageData = messageDataArgCaptor.getValue();
        var uriVariables = uriVariablesArgCaptor.getValue();

        assertEquals(DATA, messageData);
        assertEquals(CHIPS_REST_ENDPOINT, uriVariables.get(CHIPS_REST_ENDPOINT_URI_VAR));
    }
}