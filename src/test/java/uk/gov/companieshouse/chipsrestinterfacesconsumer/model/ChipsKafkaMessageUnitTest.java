package uk.gov.companieshouse.chipsrestinterfacesconsumer.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChipsKafkaMessageUnitTest {

    @Test
    void testStringValue() {
        String APP_ID = "12345";
        int ATTEMPT = 5;
        String MESSAGE_ID = "ABCDE";
        String DATA = "{someField:someValue,someField2:someValue2}";
        String CHIPS_REST_ENDPOINT = "http://server:1234/chipsRestInterfaces";
        String CREATED_AT = LocalDateTime.now().toString();

        ChipsKafkaMessage chipsKafkaMessage = new ChipsKafkaMessage();

        chipsKafkaMessage.setAppId(APP_ID);
        chipsKafkaMessage.setAttempt(ATTEMPT);
        chipsKafkaMessage.setMessageId(MESSAGE_ID);
        chipsKafkaMessage.setData(DATA);
        chipsKafkaMessage.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        chipsKafkaMessage.setCreatedAt(CREATED_AT);

        String expectedOutput = String.format("ChipsKafkaMessage{appId='%s', attempt='%d', messageId='%s', data='%s', chipsRestEndpoint='%s', createdAt='%s'}", APP_ID, ATTEMPT, MESSAGE_ID, DATA, CHIPS_REST_ENDPOINT, CREATED_AT);
        assertEquals(expectedOutput, chipsKafkaMessage.toString());
    }
}
