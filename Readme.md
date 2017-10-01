# Survey requests service [![Build Status](https://travis-ci.org/comtihon/survey_requests.svg?branch=master)](https://travis-ci.org/comtihon/survey_requests)
Gets answered questions, validates and puts to kafka.

## Run
Ensure that [Kafka](https://kafka.apache.org/) is accessible before running the service.  
Access urls are specified in application.properties for `spring.kafka.bootstrap-servers`.

### In docker

    sudo ./gradlew build buildDocker
    sudo docker run -p 8080:8080 -t com.surveyor.requests

### In OS

    ./gradlew bootRun

## Protocol
POST __/respond/{surveyId}__
with body:
    [{"id" : "answerId1"}, ... , {"id" : "answerIdN"}]
