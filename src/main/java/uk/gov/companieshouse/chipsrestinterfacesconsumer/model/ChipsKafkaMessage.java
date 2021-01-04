package uk.gov.companieshouse.chipsrestinterfacesconsumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChipsKafkaMessage {

    @JsonProperty("app_id")
    private String appId;

    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("data")
    private String data;

    @JsonProperty("chips_rest_endpoint")
    private String chipsRestEndpoint;

    @JsonProperty("created_at")
    private String createdAt;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getChipsRestEndpoint() {
        return chipsRestEndpoint;
    }

    public void setChipsRestEndpoint(String chipsRestEndpoint) {
        this.chipsRestEndpoint = chipsRestEndpoint;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ChipsKafkaMessage{" +
                "appId='" + appId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", data='" + data + '\'' +
                ", chipsRestEndpoint='" + chipsRestEndpoint + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
