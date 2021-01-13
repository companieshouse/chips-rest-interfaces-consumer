package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AvroSerializer {

    public byte[] serialize(ChipsRestInterfacesSend message, Schema schema) throws IOException {
        GenericDatumWriter<ChipsRestInterfacesSend> datumWriter = new GenericDatumWriter<>();

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
            datumWriter.setSchema(schema);
            datumWriter.write(message, encoder);
            encoder.flush();

            byte[] dataValue = stream.toByteArray();
            encoder.flush();

            return dataValue;
        }
    }
}
