# microservice-interaction-generator

# Progress Report

This application currently generates the interaction graph for microservices using the spring framework exchange method.

Steps to run -

1. Create an input directory with 4 files results0.csv, results1.csv, results2.csv ,results_meta.csv generated using the codeQl queries for the spring framework exchange method.
2. Create an output directory.
3. Run the commands -
```shell
mvn clean package

cd target

java -jar microservice-interaction-generator-1.0-SNAPSHOT-jar-with-dependencies.jar <project dir> <base dir of the project> <input dir> <output dir>

Example : java -jar microservice-interaction-generator-1.0-SNAPSHOT-jar-with-dependencies.jar ../train-ticket-fork/train-ticket train-ticket ../input ../output
```

Output -

The output will contain the following files -
1. Interaction graph of the form -
```json
{
  "ts-wait-order-service": [
    {
      "destination": "ts-preserve-service",
      "interactionBody": {
        "httpMethodType": "\"Http - POST\"",
        "url": "/api/v1/contactservice/preserve",
        "body": "WaitListOrderVO"
      }
    }
  ],
  "ts-basic-service": [
    {
      "destination": "ts-station-service",
      "interactionBody": {
        "httpMethodType": "\"Http - POST\"",
        "url": "/api/v1/stationservice/stations/idlist",
        "body": "List"
      }
    },
    {
      "destination": "ts-train-service",
      "interactionBody": {
        "httpMethodType": "\"Http - POST\"",
        "url": "/api/v1/trainservice/trains/byNames",
        "body": "List"
      }
    },...
```

2. GraphViz interaction graph input file
3. MermaidJs interaction graph input file