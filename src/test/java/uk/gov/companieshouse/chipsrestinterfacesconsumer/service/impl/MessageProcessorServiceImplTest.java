package uk.gov.companieshouse.chipsrestinterfacesconsumer.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.client.ChipsRestClient;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProcessorServiceImplTest {

    @Mock
    ChipsRestClient chipsRestClient;

    @InjectMocks
    MessageProcessorServiceImpl messageProcessorService;

    @Captor
    ArgumentCaptor<ChipsKafkaMessage> chipsKafkaMessageArgumentCaptor;

    @Test
    void processMessagesTest() {
        ChipsKafkaMessage chipsKafkaMessage1 = new ChipsKafkaMessage();
        ChipsKafkaMessage chipsKafkaMessage2 = new ChipsKafkaMessage();

        HashSet<ChipsKafkaMessage> chipsKafkaMessageHashSet = new HashSet<>();
        chipsKafkaMessageHashSet.add(chipsKafkaMessage1);
        chipsKafkaMessageHashSet.add(chipsKafkaMessage2);

        messageProcessorService.processMessages(chipsKafkaMessageHashSet);

        verify(chipsRestClient, times(chipsKafkaMessageHashSet.size())).sendToChips(chipsKafkaMessageArgumentCaptor.capture());

        assertThat(chipsKafkaMessageArgumentCaptor.getAllValues(), hasItems(chipsKafkaMessage1, chipsKafkaMessage2));
    }
}