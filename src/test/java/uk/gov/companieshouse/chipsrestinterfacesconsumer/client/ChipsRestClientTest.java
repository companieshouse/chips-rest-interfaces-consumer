package uk.gov.companieshouse.chipsrestinterfacesconsumer.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChipsRestClientTest {

    private static final String CHIPS_REST_HOST = "hostname/";
    private static final String CHIPS_REST_ENDPOINT = "test-endpoint";
    private static final String DATA = "data";
    private static final URI FULL_EXPANDED_CHIPS_REST_URL = new UriTemplate(CHIPS_REST_HOST+CHIPS_REST_ENDPOINT).expand();

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationLogger logger;

    @InjectMocks
    private ChipsRestClient chipsRestClient;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> messageDataArgCaptor;

    @Test
    void sendToChipsTest() {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setData(DATA);
        chipsRestInterfacesSend.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        ReflectionTestUtils.setField(chipsRestClient, "chipsRestHost", CHIPS_REST_HOST);
        chipsRestClient.init();
        chipsRestClient.sendToChips(chipsRestInterfacesSend);

        verify(restTemplate, times(1)).exchange(
                eq(FULL_EXPANDED_CHIPS_REST_URL), eq(HttpMethod.POST), messageDataArgCaptor.capture(),
                eq(String.class)
        );

        var messageData = messageDataArgCaptor.getValue();

        assertEquals(DATA, messageData.getBody());
        assertEquals(MediaType.APPLICATION_JSON, messageData.getHeaders().getContentType());
    }
}