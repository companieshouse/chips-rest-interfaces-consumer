package uk.gov.companieshouse.chipsrestinterfacesconsumer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChipsKafkaMessageUnitTest {

    @Test
    void testStrongValue()
    {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();
        assertEquals("hello", chipsKafkaMessage.toString());
    }
}
