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
2. Run ./bin/chs-dev modules enable chips-interfaces
3. Run ./bin/chs-dev development enable chips-rest-interfaces-consumer (this will allow you to make changes).
4. Run docker using "tilt up" in the docker-chs-development directory.
5. Use spacebar in the command line to open tilt window - wait for chips-rest-interfaces-consumer to become green.
6. Run [strike-off-objections](https://github.com/companieshouse/strike-off-objections-web) or an application of your choice that sends data to CHIPS.
7. Once you have reached the end of the journey or the point where CHIPS has been contacted look in the tilt logs for chips-rest-interfaces-consumer.
and check that the logs contain a message similar to "Message offset ... retrieved, processing".

These instructions are for a local docker environment.

### Config variables

Key             | Example Value   | Description
----------------|---------------- |------------------------------------
CHIPS_REST_INTERFACES_HOST | <CHIPS_REST_INTERFACES_URL> | Exit point to CHIPS for incoming messages.
HUMAN_LOG | 1 |
KAFKA_BROKER_ADDR | kafka:9092 |
KAFKA_GROUP_NAME | chips-rest-interfaces-consumer-group |
KAFKA_CONSUMER_TOPIC | chips-rest-interfaces-send | Directs messages to and from the main topic.
KAFKA_CONSUMER_POLL_TIMEOUT_MS | 100 |
KAFKA_RETRY_TOPIC | chips-rest-interfaces-retry | Directs messages to and from the retry topic.
KAFKA_PRODUCER_RETRIES | 5 |
LOG_LEVEL | DEBUG |
MAX_RETRY_ATTEMPTS | 10 | Number of retries before the message is added to the error topic.
RETRY_THROTTLE_RATE_SECONDS | 30 | Delay between retry consumer processing the messages on the retry topic.