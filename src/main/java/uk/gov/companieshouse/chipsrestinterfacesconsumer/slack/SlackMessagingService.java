package uk.gov.companieshouse.chipsrestinterfacesconsumer.slack;

import java.util.List;
import java.util.Map;

public interface SlackMessagingService {

    void sendMessage(List<String> failedMessageIds);
}
