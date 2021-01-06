package uk.gov.companieshouse.chipsrestinterfacesconsumer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChipsRestServiceTest {

    private static final String CHIPS_ENDPOINT = "test-endpoint";
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ChipsRestService chipsRestService;

    @Test
    void sendToChipsTest() {
        ChipsKafkaMessage chipsKafkaMessage1 = new ChipsKafkaMessage();
        chipsKafkaMessage1.setData("test1");
        chipsKafkaMessage1.setChipsRestEndpoint(CHIPS_ENDPOINT);
        ChipsKafkaMessage chipsKafkaMessage2 = new ChipsKafkaMessage();
        chipsKafkaMessage2.setData("test2");
        chipsKafkaMessage2.setChipsRestEndpoint(CHIPS_ENDPOINT);

        Collection<ChipsKafkaMessage> kafkaMessageCollection = new HashSet<>();
        kafkaMessageCollection.add(chipsKafkaMessage1);
        kafkaMessageCollection.add(chipsKafkaMessage2);

        chipsRestService.init();
        chipsRestService.sendToChips(kafkaMessageCollection);

        verify(restTemplate, times(kafkaMessageCollection.size())).postForEntity(any(), any(), eq(String.class), anyMap());
    }
}