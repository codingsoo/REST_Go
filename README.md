## 🛠️ REST_GO: Your Comprehensive Collection of REST API Testing Tools and RESTful Services

Welcome to **REST_GO**, a repository that collects REST API Testing Tools and RESTful Services. 

### 🎯 Purpose:
With the ever-growing importance of RESTful web services, effective testing methods are paramount. Our repository aims to serve as a one-stop resource for both researchers and practitioners in the realm of REST API testing. By consolidating the most valuable tools and services, we hope to advance the state of the art and ease the tasks of developers and testers alike.

### 📜 Cite Us:
If you find the resources here beneficial, we'd appreciate it if you consider citing our research paper, titled "[Automated Test Generation for REST APIs: No Time to Rest Yet](https://dl.acm.org/doi/abs/10.1145/3533767.3534401)". 

[Bibtex Citation Here](https://github.com/codingsoo/REST_Go/tree/master/docs/issta2022.bib)

### Tools

We currently have 11 open source REST API testing projects:

1. [EvoMaster](https://github.com/EMResearch/EvoMaster): Evolutionary Algorithm and Dynamic Program Analysis approach
2. [RESTler](https://github.com/microsoft/restler-fuzzer): Identifies producer-consumer dependencies among request types using OpenAPI definition
3. [RestTestGen](https://github.com/SeUniVr/RestTestGen): Generates valid call sequences by constructing and traversing an Operation Dependency Graph (ODG) based on API dependencies
4. [RESTest](https://github.com/isa-group/RESTest): Supports inter-parameter dependency specification and analysis using constraint-based testing
5. [bBOXRT]( https://eden.dei.uc.pt/~cnl/papers/2020-access.zip): Robustness testing tool for REST services, generating invalid inputs combined with valid parameters based on a service description document
6. [Schemathesis](https://github.com/schemathesis/schemathesis.git): Property-based testing approach
7. [Tcases](https://github.com/Cornutum/tcases): Models input space using operation's input and output for comprehensive testing
8. [Dredd](https://github.com/apiaryio/dredd): Validates responses based on status codes, headers, and body payloads using sample values from the specification and dummy values
9. [APIFuzzer](https://github.com/KissPeter/APIFuzzer): Employs fuzzing with random values and mutations
10. [RestCT](https://github.com/GIST-NJU/RestCT): Combinatorial testing-based approach
11. Morest: Dynamically updates RESTful-service Property Graph (RPG) to model API and object schema information (source code obtained from authors and placed in the `tools/morest` directory)

Given the frequent updates to most of these tools, I recommend visiting their official websites for the latest installation instructions.

### Services

We have identified 20 open-source Java-based RESTful services:

1. [Corona-Warn-App Verification Server](https://github.com/corona-warn-app/cwa-verification-server)
2. [ERC-20 RESTful service](https://github.com/web3labs/erc20-rest-service)
3. [Features Model MicroService](https://github.com/JavierMF/features-service)
4. [Genome Nexus](https://github.com/genome-nexus/genome-nexus)
5. [LanguageTool](https://github.com/languagetool-org/languagetool)
6. [Simple internet-market](https://github.com/aleksey-lukyanets/market)
7. [NCS](https://github.com/EMResearch/EMB/tree/master/jdk_8_maven/cs/rest/artificial/ncs)
8. [News](https://github.com/arcuri82/testing_security_development_enterprise_systems)
9. [OCVN](https://github.com/devgateway/ocvn)
10. [Person Controller](https://github.com/MaBeuLux88/java-spring-boot-mongodb-starter)
11. [Problem & Project Controller](https://github.com/phantasmicmeans/spring-boot-restful-api-example)
12. [Project Tracking System](https://github.com/SelimHorri/project-tracking-system-backend-app)
13. [proxyprint-kitchen](https://github.com/ProxyPrint/proxyprint-kitchen)
14. RESTful web service study: This service is no longer accessible.
15. [REST Countries](https://github.com/apilayer/restcountries)
16. [SCS](https://github.com/EMResearch/EMB/tree/master/jdk_8_maven/cs/rest/artificial/scs)
17. Scout API: This service is no longer accessible.
18. [Spring Boot Actuator](https://github.com/callicoder/spring-boot-actuator-demo)
19. [Spring Batch REST](https://github.com/chrisgleissner/spring-batch-rest)
20. [User Management Microservice](https://github.com/andreagiassi/microservice-rbac-user-management)

We've collected all source codes in `services` directory. 

#### Measuring Code Coverage

For a precise measurement of code coverage, follow these steps:

1. **Set Up the Tools**: Download the [Jacoco Agent](https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/0.8.7/org.jacoco.agent-0.8.7-runtime.jar) and [Jacoco CLI](https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.7/org.jacoco.cli-0.8.7-nodeps.jar). These tools will aid in the measurement process.
2. **Run the Project with Jacoco Agent**: Launch each project with the following option, replacing `{COVERAGE_PORT}` with your chosen port number:
```
-javaagent:org.jacoco.agent-0.8.7-runtime.jar=includes=*,output=tcpserver,port={COVERAGE_PORT},address=*,dumponexit=true -Dfile.encoding=UTF-8
```
3. **Run the Coverage Script**: Execute the `get_cov.sh` script:

```
sh get_cov.sh {COVERAGE_PORT}
```

This script will produce coverage files at 10-minute intervals over the span of an hour, resulting in files named:

```
10 minute: jacoco_{COVERAGE_PORT}_1.exec
20 minute: jacoco_{COVERAGE_PORT}_2.exec
30 minute: jacoco_{COVERAGE_PORT}_3.exec
40 minute: jacoco_{COVERAGE_PORT}_4.exec
50 minute: jacoco_{COVERAGE_PORT}_5.exec
60 minute: jacoco_{COVERAGE_PORT}_6.exec
```

#### Generating Coverage and Error Report

After having the coverage result, run this command to generate the coverage and error report:

```
python3 report.py {COVERAGE_PORT} {SOURCE_CODE_LOCATION}
```

Please note that you need to have all the executable Jacoco files generated in the previous step in the same directory. It will generate a `report` directory with `error.json` that contains error report and `res.csv` that contains coverage report.

#### Add authorization header

Some APIs can have an authorization header to increase the API request limit. We recommend to use [mitmproxy](https://mitmproxy.org/) to add the authorization header.

You need to add your token in authToken.py (Replce `TOKEN_HERE` with an appropriate token) and run the following command with the service URL and proxy URL. Mitmproxy accepts the request and forwards it to the specified upstream server.

```
mitmproxy --mode reverse:SERVICE_URL -p PROXY_PORT_NUMBER -s authToken.py
```

### Proof of Concepts

We provide two proof-of-concept prototypes that help to find example value, inter-parameter dependency, and linked response parameter for each request parameter.
proof-of-concept1.py takes parameter description and parameter names in the operation and produce example values and inter-parameter dependency.
proof-of-concept2.py takes request parameter names and response parameter names in the specification and produce request parameter and response parameter pairs.
Each request parameter name has three response parameter names that are top three similar names.

```
python3 tools/proof-of-concept1.py {parameter description} {parameter names}
python3 tools/proof-of-concept2.py {request parameter names} {response parameter names}
```

However, instead of this proof of concept tools, we highly recommend to read [Enhancing REST API Testing with NLP Techniques](https://dl.acm.org/doi/abs/10.1145/3597926.3598131) and use [NLP2REST](https://github.com/codingsoo/nlp2rest) as it is more advanced technique.