package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack;

import java.util.Map;

public interface SlackMessagingService {

    void sendMessage(String kafkaMessageId,
                     Map<String, Object> logMap,
                     String errorMessage);
}
