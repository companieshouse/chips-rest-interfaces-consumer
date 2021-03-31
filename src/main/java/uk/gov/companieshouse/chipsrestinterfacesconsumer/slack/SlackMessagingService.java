package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack;

import java.util.List;

public interface SlackMessagingService {

    void sendErrorTopicMessage(List<String> failedMessageIds);

    void sendDeserializationErrorMassage(String topic, int partition, long offset);

}
