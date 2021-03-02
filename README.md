# Chips Rest Interfaces Consumer

### Overview
Allows CHS services to retry failed attempts to connect to CHIPS

### Requirements

In order to run the service locally you will need the following:

- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

### Getting started

To checkout and build the service:
1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.
2. Run `./bin/chs-dev modules enable chips-interfaces`
3. Run `./bin/chs-dev development enable chips-rest-interfaces-consumer` (this will allow you to make changes).
4. Run docker using "tilt up" in the docker-chs-development directory.
5. Use spacebar in the command line to open tilt window - wait for chips-rest-interfaces-consumer to become green.
6. Run [strike-off-objections](https://github.com/companieshouse/strike-off-objections-web) or an application of your choice that sends data to CHIPS.
7. Once you have reached the end of the journey or the point where CHIPS has been contacted look in the tilt logs for chips-rest-interfaces-consumer.
and check that the logs contain a message similar to "Message offset < OFFSET > retrieved, processing".

These instructions are for a local docker environment.

### Config variables for Docker

Key             | Example Value   | Description
----------------|---------------- |------------------------------------
CHIPS_REST_INTERFACES_HOST | <CHIPS_REST_INTERFACES_URL> | Exit point to CHIPS for incoming messages.
JAVA_HOME | =/usr/lib/jvm/adoptopenjdk-11-hotspot-jre | Java 11 required so must be refeenced explicitly
KAFKA_MAIN_CONSUMER_GROUP_NAME | chips-rest-interfaces-main-consumer-group |
KAFKA_RETRY_CONSUMER_GROUP_NAME | chips-rest-interfaces-retry-consumer-group |
KAFKA_ERROR_CONSUMER_GROUP_NAME | chips-rest-interfaces-error-consumer-group |
KAFKA_CONSUMER_TOPIC | chips-rest-interfaces-send | The main topic to consume
KAFKA_CONSUMER_POLL_TIMEOUT_MS | 100 |
KAFKA_MAX_POLL_INTERVAL_MS | 300000 |
KAFKA_PRODUCER_RETRIES | 5 |
KAFKA_PRODUCER_BATCH_SIZE_BYTES | 131072 | Optimum size for handling potentially large batches of messages
KAFKA_PRODUCER_LINGER_MS | 500 | Prevents messages beings sent piecemeal by alllowing time for the Kafka producer to batch messsages together in one transaction.
PATH | $JAVA_HOME/bin:$PATH | 
MAX_RETRY_ATTEMPTS | 10 | Number of retries before the message is added to the error topic.
RETRY_THROTTLE_RATE_SECONDS | 30 | Delay between retry consumer processing the messages on the retry topic.
RUN_APP_IN_ERROR_MODE | false | This can run in two modes normal and error, this toggles the modes.

### Config Variables for Chs-Configs

CHIPS_REST_INTERFACES_HOST | <CHIPS_REST_INTERFACES_URL> | Exit point to CHIPS for incoming messages.
HUMAN_LOG | 1 |
KAFKA_BROKER_ADDR | kafka:9092 |
KAFKA_MAIN_CONSUMER_GROUP_NAME | chips-rest-interfaces-main-consumer-group |
KAFKA_RETRY_CONSUMER_GROUP_NAME | chips-rest-interfaces-retry-consumer-group |
KAFKA_ERROR_CONSUMER_GROUP_NAME | chips-rest-interfaces-error-consumer-group |
KAFKA_CONSUMER_TOPIC | chips-rest-interfaces-send | The main topic to consume
KAFKA_CONSUMER_POLL_TIMEOUT_MS | 100 |
KAFKA_MAX_POLL_INTERVAL_MS | 300000 |
KAFKA_PRODUCER_RETRIES | 5 |
LOG_LEVEL | DEBUG |
MAX_RETRY_ATTEMPTS | 10 | Number of retries before the message is added to the error topic.
RETRY_THROTTLE_RATE_SECONDS | 30 | Delay between retry consumer processing the messages on the retry topic.
RUN_APP_IN_ERROR_MODE | false | This can run in two modes normal and error, this toggles the modes.
KAFKA_PRODUCER_BATCH_SIZE_BYTES | 131072 | Optimum size for handling potentially large batches of messages
KAFKA_PRODUCER_LINGER_MS | 500 | Prevents messages beings sent piecemeal by alllowing time for the Kafka producer to batch messsages together in one transaction.

### Slack

To communicate any errors sending messages to Chips, to the Front End support team, this app uses the Slack API:

https://companieshouse.atlassian.net/wiki/spaces/TTL/pages/2652078179/Slack+Notifications

### Kafka complexities encountered and their solutions

A list can be found here:

https://companieshouse.atlassian.net/wiki/spaces/TTL/pages/2660073581/Complexities+Implementing+Kafka