package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class MessageProducerImpl implements MessageProducer {

    private final ApplicationLogger logger;

    private final SerializerFactory serializerFactory;

    private final CHKafkaProducer producer;

    @Autowired
    public MessageProducerImpl(ApplicationLogger logger,
                               SerializerFactory serializerFactory,
                               CHKafkaProducer producer) {
        this.logger = logger;
        this.serializerFactory = serializerFactory;
        this.producer = producer;
    }

    @Override
    public void writeToTopic(ChipsRestInterfacesSend chipsMessage, String topicName) throws ServiceException {
        try {
            logger.infoContext(chipsMessage.getMessageId(),
                    String.format("Writing this message to topic: %s", topicName));
            byte[] serializedData = serialize(chipsMessage);
            Message kafkaMessage = new Message();
            kafkaMessage.setValue(serializedData);
            kafkaMessage.setTopic(topicName);
            kafkaMessage.setTimestamp(Long.valueOf(chipsMessage.getCreatedAt()));
            Future<RecordMetadata> future = producer.sendAndReturnFuture(kafkaMessage);
            future.get();
        } catch (SerializationException | ExecutionException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Thread Interrupted when future was sent and returned", ie);
        }
    }

    private byte[] serialize(ChipsRestInterfacesSend chipsMessage) throws SerializationException {
        return serializerFactory
                .getSpecificRecordSerializer(ChipsRestInterfacesSend.class)
                .toBinary(chipsMessage);
    }
}
