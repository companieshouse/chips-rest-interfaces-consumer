package uk.gov.companieshouse.chipsrestinterfacesconsumer.retry;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
class AvroSerializer {

    byte[] serialize(ChipsRestInterfacesSend data) throws IOException {
        DatumWriter<ChipsRestInterfacesSend> datumWriter = new SpecificDatumWriter<>();

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.setSchema(data.getSchema());
            datumWriter.write(data, encoder);
            encoder.flush();

            byte[] serializedData = out.toByteArray();
            encoder.flush();

            return serializedData;
        }
    }
}
