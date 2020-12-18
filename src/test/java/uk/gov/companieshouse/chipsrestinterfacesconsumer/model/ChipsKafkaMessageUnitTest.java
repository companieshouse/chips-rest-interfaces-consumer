package uk.gov.companieshouse.chipsrestinterfacesconsumer.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChipsKafkaMessageUnitTest {

    @Test
    void testStringValue()
    {
        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();
        assertEquals("hello", chipsKafkaMessage.toString());
    }
}
