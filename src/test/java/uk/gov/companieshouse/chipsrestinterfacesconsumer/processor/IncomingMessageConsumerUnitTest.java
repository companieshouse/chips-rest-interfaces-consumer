package uk.gov.companieshouse.chipsrestinterfacesconsumer.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.consumer.CHConsumer;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomingMessageConsumerUnitTest {

    private static final String TEST_JSON = "{}";

    @InjectMocks
    @Spy
    private IncomingMessageConsumer incomingMessageConsumer;

    @Mock
    private ConsumerConfig config;

    @Mock
    private CHConsumer consumer;

    @Test
    void initialisationTest(){
        doReturn(consumer).when(incomingMessageConsumer).createConsumer(Mockito.any());
        ReflectionTestUtils.setField(incomingMessageConsumer, "groupName", "test-group");
        ReflectionTestUtils.setField(incomingMessageConsumer, "brokerAddress", "kafka address");
        ReflectionTestUtils.setField(incomingMessageConsumer, "topicName", "filing-received");
        ReflectionTestUtils.setField(incomingMessageConsumer, "pollTimeout", 100);
        incomingMessageConsumer.init();

        verify(consumer).connect();

        ArgumentCaptor<ConsumerConfig> consumerConfigCaptor = ArgumentCaptor.forClass(ConsumerConfig.class);
        verify(incomingMessageConsumer).createConsumer(consumerConfigCaptor.capture());
        ConsumerConfig config = consumerConfigCaptor.getValue();
        assertNotNull(config.getBrokerAddresses());
        assertEquals(1, config.getBrokerAddresses().length);

        assertEquals(ReflectionTestUtils.getField(incomingMessageConsumer, "groupName"),
                config.getGroupName());
        assertEquals(ReflectionTestUtils.getField(incomingMessageConsumer, "brokerAddress"),
                config.getBrokerAddresses()[0]);
        assertNotNull(config.getTopics());
        assertEquals(ReflectionTestUtils.getField(incomingMessageConsumer, "topicName"),
                config.getTopics().get(0));
        assertEquals(1, config.getTopics().size());
        assertEquals(ReflectionTestUtils.getField(incomingMessageConsumer, "pollTimeout"),
                config.getPollTimeout());
    }

    @Test
    void testReadNoMessage() {
        List<Message> messages = Collections.emptyList();
        when(consumer.consume()).thenReturn(messages);
        Collection<ChipsKafkaMessage> result = incomingMessageConsumer.read();
        assertTrue(result.isEmpty());
    }

    @Test
    void testReadValidMessage() {
        // TODO mock deserializer
        List<Message> messages = new ArrayList<Message>();
        when(consumer.consume()).thenReturn(messages);
        Collection<ChipsKafkaMessage> result = incomingMessageConsumer.read();
        assertTrue(result.isEmpty());
    }

    @Test
    void testDeserializeExceptionIsCaught() throws IOException {
        List<Message> messages = new ArrayList<Message>();
        when(consumer.consume()).thenReturn(messages);
        Message message = new Message();
        message.setValue(TEST_JSON .getBytes());
        messages.add(message);
        when(incomingMessageConsumer.deserialise(message)).thenThrow(new IOException());
        incomingMessageConsumer.read();
        assertThatThrownBy(() -> incomingMessageConsumer.deserialise(message))
                .isInstanceOf(IOException.class);
    }
}
