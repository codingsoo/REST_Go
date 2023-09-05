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
11. Morest: Dynamically updates RESTful-service Property Graph (RPG) to model API and object schema information (source code obtained from authors and placed in the `tool` directory)

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
14. RESTful web service study: Service Closed.
15. [REST Countries](https://github.com/apilayer/restcountries)
16. [SCS](https://github.com/EMResearch/EMB/tree/master/jdk_8_maven/cs/rest/artificial/scs)
17. Scout API: Service Closed.
18. [Spring Boot Actuator](https://github.com/callicoder/spring-boot-actuator-demo)
19. [Spring Batch REST](https://github.com/chrisgleissner/spring-batch-rest)
20. [User Management Microservice](https://github.com/andreagiassi/microservice-rbac-user-management)

Please note that Corona-Warn-App Verification Server has API rate limit 1000 per an hour now.

[//]: # (## Reproduce the result of our paper "Automated Test Generation for REST APIs: No Time to Rest Yet".)

[//]: # ()
[//]: # (### Check the basic functionality)

[//]: # ()
[//]: # (In this section, we give detailed instructions to check the basic functionality of this artifact.)

[//]: # (We show how to use the REST-API testing tools used in our study to test the project-tracking-system service.)

[//]: # ()
[//]: # (### Requirements)

[//]: # ()
[//]: # (We used Google Cloud e2-standard-4 machines running Ubuntu 20.04 for this project, but a Linux environment with the software listed below installed would work. Note that the setup script is tested in Google Cloud e2-standard-4 &#40;Ubuntu 20.04&#41;.)

[//]: # ()
[//]: # (- Required software: Dotnet 5, Java8, Java11, Git, Sudo, Wget, zip, unzip, MVN 3.5, Gradle 6.7, Python3.8-pip, Virtualenv, NodeJS v10.19, and Docker 20.10. )

[//]: # ()
[//]: # (### Setup)

[//]: # ()
[//]: # (You can manually install the required software. Or, if you use the same testing environment that we used, you can run the command below for setup.)

[//]: # ()
[//]: # (```)

[//]: # (cd REST_Go # Go to the root directory)

[//]: # (sh small_setup.sh)

[//]: # (```)

[//]: # ()
[//]: # (It will take around 20 minutes to finish setup.)

[//]: # ()
[//]: # ()
[//]: # (### Run)

[//]: # ()
[//]: # (You can test the service with a tool using `run_small.py`, a python script to test the service for six minutes.)

[//]: # (To use this script, you should provide the tool's name and a port number. Possible tools' names are`evomaster-whitebox`, `evomaster-blackbox`, `restler`, `restest`, `resttestgen`, `bboxrt`, `schemathesis`, `dredd`, `tcases`, and `apifuzzer`.)

[//]: # (You can use any available port number. You should use a different port number for each run, otherwise the code coverage will be based on the previous runs. The port number is used for collecting the achieved code coverage.)

[//]: # (Before running the script, make sure that you use `virtualenv`.)

[//]: # (Also, we need to check if there is already running session. You can check the running sessions using "tmux ls" command. If there is running session, you should kill the session before running a new experiment.)

[//]: # (You can kill the session with "tmux kill-sess -t {session name}." You should find the session name in "tmux ls" command if there is any.)

[//]: # ()
[//]: # (```)

[//]: # (source venv/bin/activate)

[//]: # (python3 run_small.py {tool_name} {port_number})

[//]: # (```)

[//]: # ()
[//]: # (It will automatically start the service and the script for getting coverage.)

[//]: # ()
[//]: # (### Generate Report)

[//]: # ()
[//]: # (We provide a python script to generate a report showing the code coverage achieved and bug founds. You can see the result in data/{service name}/res.csv.)

[//]: # (You can also find the detailed error message and time log in data/{service name}/error.json and data/{service name}/time.json.)

[//]: # ()
[//]: # (```)

[//]: # (python3 report_small.py {port_number})

[//]: # (```)

[//]: # ()
[//]: # (In data/{service name}/res.csv, there are seven rows and three columns.)

[//]: # (The first six rows show the coverage achieved in time from 1-6 minutes. In each of these rows, the columns show, in percentage, the line, branch, and method coverage.)

[//]: # (The last row presents the errors found with the columns showing the number of found errors, unique errors, and library errors.)

[//]: # ()
[//]: # (### Stop service)

[//]: # ()
[//]: # (Users can stop a service using the following command.)

[//]: # ()
[//]: # (```)

[//]: # (python3 stop_service.py {service name})

[//]: # (```)

[//]: # ()
[//]: # (### An Example)

[//]: # ()
[//]: # (One can use the commands below to test the service project-tracking-system with RESTler and obtain the result.)

[//]: # ()
[//]: # (```)

[//]: # (cd REST_Go)

[//]: # (sh small_setup.sh)

[//]: # (source venv/bin/activate)

[//]: # (python run_small.py restler 10200)

[//]: # (python report_small.py 10200)

[//]: # (```)

[//]: # ()
[//]: # (If you check the data/project-tracking-system/res.csv, you will see the sixth row for six minutes code coverage &#40;which should be something similar to "35%, 7.3%, 4.7%"&#41; and the seventh row for the number of bugs found &#40;which should be something similar to "35, 7, 5"&#41;.)

[//]: # ()
[//]: # (## Detailed Description)

[//]: # ()
[//]: # ()
[//]: # (### Setup)

[//]: # ()
[//]: # (In our study, we ran all the experiments on Google Cloud e2-standard-4 machines running Ubuntu 20.04. Each machine has four 2.2GHz Intel-Xeon processors and 16GB RAM. The major dependencies that we used are Java8, Java11, Python3.8, NodeJS v10.19, and Docker 20.10. We provide a setup script that sets up the environment, tools, and services. Please note that we used Ubuntu 20.04 environment. We also tested this artifact in Debian 10 and MacOS 12. We didn't prepare automated setup scripts for these environments, but plan to do in the future. The setup script needs around 2 hours to finish.)

[//]: # (```)

[//]: # (sh setup.sh)

[//]: # (```)

[//]: # ()
[//]: # (We have configured the databases needed for testing the services using Docker. This is automatically done in the setup script. For one of the services, ERC20-rest-service, users need to manually set Private Ethereum network using the commands below.)

[//]: # (```)

[//]: # (tmux new -s ether # Create a session for ethereum)

[//]: # (geth --datadir ethereum init genesis.json)

[//]: # (geth --networkid 42 --datadir ethereum --http --http.port 8545 --http.corsdomain "*" --http.api "admin,db,eth,debug,miner,net,shh,txpool,personal,web3" --port 30303 --mine --allow-insecure-unlock console)

[//]: # (>> personal.unlockAccount&#40;"05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11"&#41;)

[//]: # (>> miner.start&#40;1&#41; # wait until mine process starts)

[//]: # (>> personal.unlockAccount&#40;"05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11"&#41;)

[//]: # (# press ctrl + b + d to detach the session)

[//]: # (```)

[//]: # ()
[//]: # (Now you are ready to run the experiment!)

[//]: # ()
[//]: # (### How to run the tool?)

[//]: # ()
[//]: # (You can use the following tools `EvoMasterWB`, `EvoMasterBB`, `RESTler`, `RESTest`, `RestTestGen`, `bBOXRT`, `Schemathesis`, `Dredd`, `Tcases`, and `APIFuzzer` to test, using our python script, the following services `cwa-verification`, `erc20-rest-service`, `features-service`, `genome-nexus`, `languagetool`, `market`, `ncs`, `news`, `ocvn`, `person-controller`, `problem-controller`, `project-tracking-system`, `proxyporint`, `rest-study`, `restcountries`, `scout-api`, `scs`, `spring-batch-rest`, `spring-boot-sample-app`, and `user-management`.)

[//]: # (You can use any available port number, but make sure to use different port numbers for different runs. The port number is used for collecting the achieved code coverage.)

[//]: # (Before run the script, make sure that you use the `virtualenv`.)

[//]: # (Also, we need to check if there is already running session. You can check the running sessions using "tmux ls" command. If there is running session, you may want to kill the session before running new experiment.)

[//]: # (You can kill the session with "tmux kill-sess -t {session name}." You should find the session name in "tmux ls" command if there is any.)

[//]: # (```)

[//]: # (python3 run_tool.py {tool_name} {service_name} {time_limit})

[//]: # (```)

[//]: # ()
[//]: # (### Generate a report.)

[//]: # ()
[//]: # (You can use the command below to produce a report containing the testing result.)

[//]: # ()
[//]: # (```)

[//]: # (python3 report.py {port number} {service name})

[//]: # (```)

[//]: # ()
[//]: # (The report has seven rows and three columns. )

[//]: # (The first six rows show the coverage results achieved in time obtained from 10, 20, 30, 40, 50, and 60 minutes. In each of these rows, the columns show, in percentage, the line, branch, and method coverage achieved. The last row presents the errors found with the columns showing the number of found errors, unique errors, and library errors.)

[//]: # (You can compare this result to our result in `Result` section.)

[//]: # ()
[//]: # (### Stop service)

[//]: # ()
[//]: # (Users can stop a service using the following command.)

[//]: # ()
[//]: # (```)

[//]: # (python3 stop_service.py {service name})

[//]: # (```)

[//]: # ()
[//]: # (### Run all steps )

[//]: # ()
[//]: # (We provide a script to run a tool for all services. The run.py runs all steps except for setup and report.)

[//]: # ()
[//]: # (```)

[//]: # (python3 run.py {tool name})

[//]: # (```)

[//]: # ()
[//]: # (### Result)

[//]: # ()
[//]: # (You can compare your result to our result below. This figure, although not shown in the paper, is generated based on the same experiment results. Since the tools have randomness, your results may be different from ours. Note that we ran each tool ten times and computed the average. )

[//]: # ()
[//]: # (![res]&#40;images/figure_all.png&#41;)

[//]: # ()
[//]: # (1: Evo-White, 2: RESTler, 3: RestTestGen, 4: RESTest, 5: bBOXRT , 6: Schemathesis, 7: Tcases, 8: Dredd, 9: Evo-Black, 10: APIFuzzer, A: Features-Service, B: Languagetool, C: NCS, D: News, E: OCVN, F: ProxyPrint, G: Restcountries, H: Scout-API, I: SCS, J: ERC20-Rest-Service, K: Genome-Nexus, L: Person-Controller, M: Problem-Controller, N: Rest-Study, O: Spring-Batch-Rest, P: Spring-Boot-Sample-App, Q: User-Management, R: CWA-Verification, S: Market, T: Project-Tracking-System. The color of the bar represents the running time - 10 min: ![10min]&#40;images/10min.png&#41;, 20 min: ![20min]&#40;images/20min.png&#41;, 30 min: ![30min]&#40;images/30min.png&#41;, 40 min: ![40min]&#40;images/40min.png&#41;, 50 min: ![50min]&#40;images/50min.png&#41;, 60 min: ![1h]&#40;images/1h.png&#41;, and 24 hr: ![24h]&#40;images/24h.png&#41;.)

[//]: # ()
[//]: # (### Add authorization header)

[//]: # ()
[//]: # (Some APIs can have an authorization header to increase the API request limit. We recommend to use [mitmproxy]&#40;https://mitmproxy.org/&#41; to add the authorization header.)

[//]: # (You need to add your token in authToken.py and run the following command with the service URL and proxy URL. Mitmproxy accepts the request and forwards it to the specified upstream server.)

[//]: # ()
[//]: # (```)

[//]: # (mitmproxy --mode reverse:SERVICE_URL -p PROXY_PORT_NUMBER -s authToken.py)

[//]: # (```)

[//]: # ()
[//]: # (### Proof of Concepts)

[//]: # ()
[//]: # (We provide two proof-of-concept prototypes that help to find example value, inter-parameter dependency, and linked response parameter for each request parameter.)

[//]: # (proof-of-concept1.py takes parameter description and parameter names in the operation and produce example values and inter-parameter dependency.)

[//]: # (proof-of-concept2.py takes request parameter names and response parameter names in the specification and produce request parameter and response parameter pairs.)

[//]: # (Each request parameter name has three response parameter names that are top three similar names.)

[//]: # ()
[//]: # (```)

[//]: # (python3 proof-of-concept1.py {parameter description} {parameter names})

[//]: # (python3 proof-of-concept2.py {request parameter names} {response parameter names})

[//]: # (```)
