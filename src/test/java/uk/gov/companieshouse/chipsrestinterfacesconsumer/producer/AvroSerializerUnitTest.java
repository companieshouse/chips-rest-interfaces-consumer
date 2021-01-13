package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
class AvroSerializerUnitTest {

    private static final String APP_ID = "chips-rest-interfaces-consumer";
    private static int ATTEMPT = 4;
    private static final String MESSAGE_ID = "abc";
    private static final String DATA = "{subject: testing}";
    private static final String CHIPS_REST_ENDPOINT = "http://nowhere:1234";
    private static final String CREATED_AT = "01 Jan 2021 08:00:00";

    @InjectMocks
    private AvroSerializer avroSerializer;

    @Test
    void testAvroSerializer()
            throws IOException {
        Schema schema = getDummySchema(this.getClass().getClassLoader().getResource(
                "producer/chips-rest-interfaces-send.avsc"));
        ChipsKafkaMessage message = buildChipsMessageContent();

        byte[] byteArray = avroSerializer.serialize(message, schema);
        String result = decode(schema, byteArray);
        assertTrue(result.contains(APP_ID));
        assertTrue(result.contains(String.valueOf(ATTEMPT)));
        assertTrue(result.contains(MESSAGE_ID));
        assertTrue(result.contains(DATA));
        assertTrue(result.contains(CHIPS_REST_ENDPOINT));
        assertTrue(result.contains(CREATED_AT));
    }

    private String decode(Schema schema, byte[] byteArray) throws IOException {
        DatumReader<GenericRecord> reader = new GenericDatumReader<>(schema);
        ByteArrayInputStream in = new ByteArrayInputStream(byteArray);
        Decoder decoder = DecoderFactory.get().binaryDecoder(in, null);

        try {
            GenericRecord datum = reader.read(null, decoder);
            return datum.toString();
        } catch (AvroRuntimeException | ArrayIndexOutOfBoundsException adex) {
            fail("Malformed message - Failed to deserialize", adex);
        } finally {
            in.close();
        }
        return null;
    }

    private Schema getDummySchema(URL url) throws IOException {
        String avroSchemaPath = Objects.requireNonNull(url).getFile();
        Schema.Parser parser = new Schema.Parser();
        return parser.parse(new File(avroSchemaPath));
    }

    private ChipsKafkaMessage buildChipsMessageContent() {
        ChipsKafkaMessage message = new ChipsKafkaMessage();
        message.setAppId(APP_ID);
        message.setAttempt(ATTEMPT);
        message.setMessageId(MESSAGE_ID);
        message.setData(DATA);
        message.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        message.setCreatedAt(CREATED_AT);
        return message;
    }

}
