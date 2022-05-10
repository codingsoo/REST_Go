# Automated Test Generation for REST APIs: No Time to Rest Yet

## Getting Started

### Check the basic functionality

In this section, we give detailed instructions to check the basic functionality of this artifact.
We show how to use the REST-API testing tools used in our study to test the project-tracking-system service.

### Requirements

We used Google Cloud e2-standard-4 machines running Ubuntu 20.04 for this project, but a Linux environment with the software listed below installed would work. Note that the setup script is tested in Google Cloud e2-standard-4 (Ubuntu 20.04).

- Required software: Java8, Java11, Git, Sudo, Wget, zip, unzip, MVN, Gradle, Python3.8-pip, Virtualenv, NodeJS v10.19, and Docker 20.10. 

### Setup

You can manually install the required software. Or, if you use the same testing environment that we used, you can run the command below for setup.

```
cd REST_Go # Go to the root directory
sh small_setup.sh
```

It will take around 20 minutes to finish setup.


### Run

You can test the service with a tool using `run_small.py`, a python script to test the service for six minutes.
To use this script, you should provide the tool's name and a port number. Possible tools' names are`evomaster-whitebox`, `evomaster-blackbox`, `restler`, `restest`, `resttestgen`, `bboxrt`, `schemathesis`, `dredd`, `tcases`, and `apifuzzer`.
You can use any available port number. The port number is used for collecting the achieved code coverage.
Before running the script, make sure that you use `virtualenv`.

```
source venv/bin/activate
python3 run_small.py {tool_name} {port_number}
```

It will automatically start the service and the script for getting coverage. You can check all the runing sessions with the command "tmux ls" and kill any session with the command "tmux kill-sess -t {session name}."

### Generate Report

We provide a python script to generate a report showing the code coverage achieved and bug founds. You can see the result in data/{service name}/res.csv!
You can also find the detailed error message and time log in data/{service name}/error.json and data/{service name}/time.json.

```
python3 report_small.py {port_number}
```

In data/{service name}/res.csv, there are seven rows and three columns.
The first six rows show the coverage achieved in time from 1-6 minutes. In each of these rows, the columns show, in percentage, the line, branch, and method coverage.
The last row presents the errors found with the columns showing the number of found errors, unique errors, and library errors.

### Stop service

Users can stop a service using the following command.

```
python3 stop_service.py {service name}
```

## Detailed Description


### Setup

In our study, we ran all the experiments on Google Cloud e2-standard-4 machines running Ubuntu 20.04. Each machine has four 2.2GHz Intel-Xeon processors and 16GB RAM. The major dependencies that we used are Java8, Java11, Python3.8, NodeJS v10.19, and Docker 20.10. We provide a setup script that sets up the environment, tools, and services. Please note that we used Ubuntu 20.04 environment. We also tested this artifact in Debian 10 and MacOS 12. We didn't prepare automated setup scripts for these environments, but plan to do in the future. The setup script needs around 2 hours to finish.
```
sh setup.sh
```

We have configured the databases needed for testing the services using Docker. This is automatically done in the setup script. For one of the services, ERC20-rest-service, users need to manually set Private Ethereum network using the commands below.
```
tmux new -s ether # Create a session for ethereum
geth --datadir ethereum init genesis.json
geth --networkid 42 --datadir ethereum --http --http.port 8545 --http.corsdomain "*" --http.api "admin,db,eth,debug,miner,net,shh,txpool,personal,web3" --port 30303 --mine --allow-insecure-unlock console
>> personal.unlockAccount("05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11")
>> miner.start(1) # wait until mine process starts
>> personal.unlockAccount("05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11")
# press ctrl + b + d to detach the session
```

Now you are ready to run the experiment!

### How to run the tool?

You can use the following tools `EvoMasterWB`, `EvoMasterBB`, `RESTler`, `RESTest`, `RestTestGen`, `bBOXRT`, `Schemathesis`, `Dredd`, `Tcases`, and `APIFuzzer` to test, using our python script, the following services `cwa-verification`, `erc20-rest-service`, `features-service`, `genome-nexus`, `languagetool`, `market`, `ncs`, `news`, `ocvn`, `person-controller`, `problem-controller`, `project-tracking-system`, `proxyporint`, `rest-study`, `restcountries`, `scout-api`, `scs`, `spring-batch-rest`, `spring-boot-sample-app`, and `user-management`.
For testing, you can use any free port number. The port number is for collecting code coverage.
Before run the script, make sure that you use the `virtualenv`.
```
python3 run_tool.py {tool_name} {service_name} {time_limit}
```

### Generate a report.

You can use the command below to produce a report containing the testing result.

```
python3 report.py {port number} {service name}
```

The report has seven rows and three columns. 
The first six rows show the coverage results achieved in time obtained from 10, 20, 30, 40, 50, and 60 minutes. In each of these rows, the columns show, in percentage, the line, branch, and method coverage achieved. The last row presents the errors found with the columns showing the number of found errors, unique errors, and library errors.
You can compare this result to our result in `Result` section.

### Stop service

Users can stop a service using the following command.

```
python3 stop_service.py {service name}
```

### Result

You can compare your result to our result below. Since the tools have randomness, you may have a slightly different result from us. We run each tool ten times and get their average.

![res](images/figure_all.png)

1: Evo-White, 2: RESTler, 3: RestTestGen, 4: RESTest, 5: bBOXRT , 6: Schemathesis, 7: Tcases, 8: Dredd, 9: Evo-Black, 10: APIFuzzer, A: Features-Service, B: Languagetool, C: NCS, D: News, E: OCVN, F: ProxyPrint, G: Restcountries, H: Scout-API, I: SCS, J: ERC20-Rest-Service, K: Genome-Nexus, L: Person-Controller, M: Problem-Controller, N: Rest-Study, O: Spring-Batch-Rest, P: Spring-Boot-Sample-App, Q: User-Management, R: CWA-Verification, S: Market, T: Project-Tracking-System. The color of the bar represents the running time - 10 min: ![10min](images/10min.png), 20 min: ![20min](images/20min.png), 30 min: ![30min](images/30min.png), 40 min: ![40min](images/40min.png), 50 min: ![50min](images/50min.png), 60 min: ![1h](images/1h.png), and 24 hr: ![24h](images/24h.png).
