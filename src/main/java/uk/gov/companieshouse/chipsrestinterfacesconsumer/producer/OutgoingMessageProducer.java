package uk.gov.companieshouse.chipsrestinterfacesconsumer.producer;

import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.common.ApplicationLogger;
import uk.gov.companieshouse.chipsrestinterfacesconsumer.model.ChipsKafkaMessage;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class OutgoingMessageProducer implements MessageProducer {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private ApplicationLogger logger;

    private AvroSerializer avroSerializer;

    private CHKafkaProducer producer;

    @Autowired
    @Qualifier("chips-kafka-send")
    private Schema schema;

    @Value("${kafka.retry.topic}")
    private String retryTopicName;

    @Autowired
    public OutgoingMessageProducer(ApplicationLogger logger,
                                   AvroSerializer avroSerializer,
                                   CHKafkaProducer producer){
        this.logger = logger;
        this.avroSerializer = avroSerializer;
        this.producer = producer;
    }


    @Override
    public void writeToQueue(ChipsKafkaMessage chipsMessage) throws ServiceException {
        try {
            Message kafkaMessage = new Message();
            byte[] serializedData = avroSerializer.serialize(chipsMessage, schema);
            kafkaMessage.setValue(serializedData);
            kafkaMessage.setTopic(retryTopicName);
            kafkaMessage.setTimestamp(LocalDateTime.parse(chipsMessage.getCreatedAt(),
                    formatter).atZone(ZoneId.systemDefault()).toEpochSecond());
            Future<RecordMetadata> future = producer.sendAndReturnFuture(kafkaMessage);
            future.get();
        } catch (IOException | ExecutionException e) {
            throw new ServiceException(e.getMessage());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Thread Interrupted when future was sent and returned");
        }
    }
}
