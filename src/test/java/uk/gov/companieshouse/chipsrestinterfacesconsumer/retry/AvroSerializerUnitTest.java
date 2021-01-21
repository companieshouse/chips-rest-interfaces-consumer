package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AvroSerializerUnitTest {

    private static final String APP_ID = "chips-rest-interfaces-consumer";
    private static final String MESSAGE_ID = "abc";
    private static final String DATA = "{subject: testing}";
    private static final String CHIPS_REST_ENDPOINT = "http://nowhere:1234";
    private static final String CREATED_AT = "1610543925";
    private static final int ATTEMPT = 4;
    @InjectMocks
    private AvroSerializer avroSerializer;

    @Test
    void testAvroSerializer() throws IOException {
        ChipsRestInterfacesSend messageContent = buildChipsMessageContent();

        byte[] byteArray = avroSerializer.serialize(messageContent);

        ChipsRestInterfacesSend result = decode(messageContent.getSchema(), byteArray);
        assertEquals(APP_ID, result.getAppId());
        assertEquals(ATTEMPT, result.getAttempt());
        assertEquals(MESSAGE_ID, result.getMessageId());
        assertEquals(DATA, result.getData());
        assertEquals(CHIPS_REST_ENDPOINT, result.getChipsRestEndpoint());
        assertEquals(CREATED_AT, result.getCreatedAt());
    }

    private ChipsRestInterfacesSend decode(Schema schema, byte[] byteArray) throws IOException {
        DatumReader<ChipsRestInterfacesSend> reader = new SpecificDatumReader<>(schema);

        try (ByteArrayInputStream in = new ByteArrayInputStream(byteArray)) {
            Decoder decoder = DecoderFactory.get().binaryDecoder(in, null);

            return reader.read(null, decoder);
        }
    }

    private ChipsRestInterfacesSend buildChipsMessageContent() {
        ChipsRestInterfacesSend message = new ChipsRestInterfacesSend();
        message.setAppId(APP_ID);
        message.setAttempt(ATTEMPT);
        message.setMessageId(MESSAGE_ID);
        message.setData(DATA);
        message.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        message.setCreatedAt(CREATED_AT);
        return message;
    }

}
