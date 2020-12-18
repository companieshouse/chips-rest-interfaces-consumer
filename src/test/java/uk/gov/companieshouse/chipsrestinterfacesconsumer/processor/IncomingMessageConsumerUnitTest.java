package uk.gov.companieshouse.chipsrestinterfacesconsumer.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.consumer.CHConsumer;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomingMessageConsumerUnitTest {

    @Mock
    private CHConsumer consumer;

    @InjectMocks
    private IncomingMessageConsumer incomingMessageConsumer;

    @Test
    void initialisationTest() {
        incomingMessageConsumer.init();
        verify(consumer).connect();
    }

    @Test
    void destroyTest() {
        incomingMessageConsumer.close();
        verify(consumer).close();
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
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        // TODO have a real message or something resembling it
        message.setValue("{}".getBytes());
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);
        Collection<ChipsKafkaMessage> result = incomingMessageConsumer.read();
        assertEquals(1, result.size());
        assertNotNull(result.iterator().next());
    }

    @Test
    void testDeserializeExceptionIsCaught() {
        List<Message> messages = new ArrayList<>();
        Message message = new Message();
        message.setValue("bad message".getBytes());
        messages.add(message);
        when(consumer.consume()).thenReturn(messages);

        incomingMessageConsumer.read();
        assertThatThrownBy(() -> incomingMessageConsumer.deserialise(message))
                .isInstanceOf(IOException.class);
        // TODO mock logger (autowire it in class being tested) and verify it is called when exception caught in read()
    }
}
