package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack;

import java.util.List;

public interface SlackMessagingService {

    void sendMessage(List<String> failedMessageIds);
}
