package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack;

import java.util.List;

public interface SlackMessagingService {

    void sendErrorTopicMessage(List<String> failedMessageIds);

    void sendDeserializationErrorMessage(String deserializationFailureMessage);

}
