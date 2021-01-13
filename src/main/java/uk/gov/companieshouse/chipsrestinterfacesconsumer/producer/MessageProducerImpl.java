package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class MessageProducerImpl implements MessageProducer {

    private ApplicationLogger logger;

    private AvroSerializer avroSerializer;

    private CHKafkaProducer producer;

    @Autowired
    private Schema schema;

    @Value("${kafka.retry.topic}")
    private String retryTopicName;

    @Autowired
    public MessageProducerImpl(ApplicationLogger logger,
                               AvroSerializer avroSerializer,
                               CHKafkaProducer producer){
        this.logger = logger;
        this.avroSerializer = avroSerializer;
        this.producer = producer;
    }

    @Override
    public void writeToTopic(ChipsKafkaMessage chipsMessage) throws ServiceException {
        try {
            logger.info(String.format("Writing message to topic: %s", retryTopicName));
            byte[] serializedData = avroSerializer.serialize(chipsMessage, schema);
            Message kafkaMessage = new Message();
            kafkaMessage.setValue(serializedData);
            kafkaMessage.setTopic(retryTopicName);
            kafkaMessage.setTimestamp(Long.valueOf(chipsMessage.getCreatedAt()));
            Future<RecordMetadata> future = producer.sendAndReturnFuture(kafkaMessage);
            future.get();
        } catch (IOException | ExecutionException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Thread Interrupted when future was sent and returned", ie);
        }
    }
}
