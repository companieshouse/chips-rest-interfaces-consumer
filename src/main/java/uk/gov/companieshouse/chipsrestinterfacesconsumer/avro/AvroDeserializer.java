package uk.gov.companieshouse.chipsrestinterfacesconsumer.avro;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    protected final Class<T> targetType;

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            T result = null;

            if (data != null) {
                DatumReader<GenericRecord> datumReader =
                        new SpecificDatumReader<>(targetType.getDeclaredConstructor().newInstance().getSchema());
                Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);

                result = (T) datumReader.read(null, decoder);
            }
            return result;
        } catch (Exception ex) {
            throw new SerializationException(
                    "Can't deserialize data from topic '" + topic + "'", ex);
        }
    }
}
