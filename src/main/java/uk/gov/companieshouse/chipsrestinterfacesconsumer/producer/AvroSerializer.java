package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AvroSerializer {

    public byte[] serialize(ChipsKafkaMessage message, Schema schema) throws IOException {
        GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        datumWriter.write(buildAvroGenericRecord(message, schema), encoder);
        encoder.flush();
        byte[] dataValue = stream.toByteArray();
        stream.close();
        return dataValue;
    }

    private GenericRecord buildAvroGenericRecord(ChipsKafkaMessage message, Schema schema) {

        GenericRecord documentData = new GenericData.Record(schema);
        documentData.put("app_id", message.getAppId());
        documentData.put("attempt", message.getAttempt());
        documentData.put("message_id", message.getMessageId());
        documentData.put("data", message.getData());
        documentData.put("chips_rest_endpoint", message.getChipsRestEndpoint());
        documentData.put("created_at", message.getCreatedAt());
        return documentData;
    }
}
