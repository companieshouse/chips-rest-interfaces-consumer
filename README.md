# Chips Rest Interfaces Consumer

### Overview
Allows CHS services to asynchronously send data to Chips-Rest-Interfaces via Kafka.  
The Chips Rest Interfaces Consumer will read messages off of a kafka topic and attempt to send them to Chips Rest Interfaces using REST.  
If these requests fail, the consumer will try them again later.

This allows CHS Services to continue running and accepting user requests even whilst CHIPS is down, possibly due to an upgrade or unexpected downtime.  


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

### Config variables

Key             | Example Value   | Description
----------------|---------------- |------------------------------------
BATCH_FAILURE_RETRY_SLEEP_MS | 1000 | How many ms to wait before retrying a batch that fails
CHIPS_REST_INTERFACES_HOST | <CHIPS_REST_INTERFACES_URL> | Exit point to CHIPS for incoming messages.
FEATURE_FLAG_SLACK_MESSAGES_020321 | true | Whether to send failure messages to a slack channel defined by SLACK_CHANNEL
HUMAN_LOG | 1 |
KAFKA_BROKER_ADDR | kafka:9092 |
KAFKA_GROUP_NAME | chips-rest-interfaces-consumer-group |
KAFKA_CONSUMER_TOPIC | chips-rest-interfaces-send | The main topic to consume
KAFKA_CONSUMER_POLL_TIMEOUT_MS | 100 |
KAFKA_PRODUCER_RETRIES | 5 |
LOG_LEVEL | DEBUG |
MAX_RETRY_ATTEMPTS | 10 | Number of retries before the message is added to the error topic.
RETRY_THROTTLE_RATE_SECONDS | 30 | Delay between retry consumer processing the messages on the retry topic.
RUN_APP_IN_ERROR_MODE | true | If true consumer only consumes off of error topic, if false, consumes off of main and retry topic
SLACK_ACCESS_TOKEN | ABCDEFGHIJKLMNOPQRSTUVWXYZ | Access token for sending slack messages
SLACK_CHANNEL | chips-rest-alerts-test | Slack channel failure messages are sent to

### How it works RUN_APP_IN_ERROR_MODE = false

1. A producer should produce a message to the KAFKA_CONSUMER_TOPIC conforming to the following [schema](https://github.com/companieshouse/chs-kafka-schemas/blob/master/schemas/chips-rest-interfaces-send.avsc)   
2. The Chips-Rest-Interfaces-Consumer will then deserialize the message and attempt to send the message via REST onto the chips-rest-interfaces with the endpoint defined in the chips-rest-endpoint field in the message  
3. If the message fails to receive a 2XX response from the REST request the message will be added to the retry topic with its attempts set to 1  
4. Messages on the retry topic are tried every RETRY_THROTTLE_RATE_SECONDS if the message still fails, a new message is added to the retry topic with its attempts incremented by 1  
5. If attempts > MAX_RETRY_ATTEMPTS the message is added to the error topic and is not automatically retried  

### How it works RUN_APP_IN_ERROR_MODE = true

1. When the consumer starts it will poll the error topic.
2. The consumer will process any messages created before the consumer was started
3. It will attempt to send the message to CHIPS_REST_INTERFACES_HOST if the message fails the consumer will add a new message to the retry topic with attempts = 1
4. An app running with `RUN_APP_IN_ERROR_MODE = false` will then read and process that message

### Topics
`chips-rest-interfaces-send`: the main topic. This is where messages to be sent to Chips Rest are put from CHS services.  
`chips-rest-interfaces-send-retry`: the retry topic. This is where Chips Rest Interfaces Consumer puts messages that fail their initial attempt.  
`chips-rest-interfaces-send-error`: the error topic. Messages that have failed all retry attempts are placed here, and are not automatically tried again.