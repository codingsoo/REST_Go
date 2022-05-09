# Automated Test Generation for REST APIs: No Time to Rest Yet

## Getting Started

### Check the basic functionality

In this section, we give detailed instructions to check the basic functionality of this artifact.
We show how our tool works with project-tracking-system service.

### Requirements

We used Google Cloud e2-standard-4 machines running Ubuntu 20.04 for this project, but any platform with the requirements below would work.
The setup script is provided for Google Cloud e2-standard-4 (Ubuntu 20.04).

- Hardware: You need at least 12GB of RAM. 
- Software: Java8, Java11, Git, Sudo, Wget, zip, unzip, MVN, Gradle, Python3.8-pip, Virtualenv, NodeJS v10.19, and Docker 20.10. 

### Setup

You can manually set up the environment, but if you use the same environment with us, you can simply type the command below

```
cd REST-Go # Go to the root directory
sh small_setup.sh
```

It will take around 18 minutes and 30 seconds. Other environment needs to handle the dependencies manually.

### Run

You can run the service and tool using run_small.py. The python script runs the service for six minutes.
You can select tool name and port name. Possible tool name can be: evomaster-whitebox, evomaster-blackbox, restler, restest, resttestgen, bboxrt, schemathesis, dredd, tcases, and apifuzzer.
You can pick any free port number for the port name. The port number is for collecting the achieved code coverage.
Before run the script, make sure you are using the virtualenv.

```
source venv/bin/activate
python3 run_small.py {tool_name} {port_number}
```

It will automatically start the service and coverage collecting script. You can check them with the command "tmux ls" and kill them with the command "tmux kill-sess -t {session name}."

### Generate Report

We automatically generate the report for the experiment. You can see the result in data/{service name}/res.csv!

```
python3 report_small.py {port_number}
```

## Detailed Description


### Setup

We used Google Cloud e2-standard-4 machines running Ubuntu 20.04. Each machine has four 2.2GHz Intel-Xeon processors and 16GB RAM. The major dependencies that we used are Java8, Java11, Python3.8, NodeJS v10.19, and Docker 20.10. We provide a setup script that setups the environment, tools, and services. Please note that we used Ubuntu 20.04 environment. We also tested this artifact in Debian 10 and MacOS 12, but we don't provide automated setup script for those environments.

The setup script needs around 2 hours.
```
sh setup.sh
```

We have configured the each database for the service using the Docker, and that is automatically done in our script. However, users need to manually set Private Ethereum network for ERC20-rest-service.
To setup the Ethereum network, users can follow the commands below.
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

### How to run the service?

You can run the service cwa-verification, erc20-rest-service, features-service, genome-nexus, languagetool, market, ncs, news, ocvn, person-controller, problem-controller, project-tracking-system, proxyporint, rest-study, restcountries, scout-api, scs, spring-batch-rest, spring-boot-sample-app, and user-management through our python script.
You can pick any free port number for the port name. The port number is for collecting the achieved code coverage.
Before run the script, make sure you are using the virtualenv.
```
source venv/bin/activate
python3 run_service.py {service_name} {port number} {whitebox} # If you want to run blackbox tools, type blackbox instead of whitebox
```

It will automatically start the service and coverage collecting script. You can check them with the command "tmux ls" and kill them with the command "tmux kill-sess -t {session name}."

### How to get achieved code coverage?

You can collect the achieved code coverage. You can select 1. The number of code coverage report 2. time in minutes. For example, if you set the number 6 and time 10, it will collect the achieved code coverage every 10 minute for 6 times. The user will have a code coverage report for 10m, 20m, 30m, 40m, 50m, and 1h.

```
tmux new -s cov # Create a session for collecting coverage
sh get_cov.sh {jacoco_port} {number of interval} {minutes between intervals}
# press ctrl + b + d to detach the session
```

### How to run the tool?

You can run EvoMasterWB, EvoMasterBB, RESTler, RESTest, RestTestGen, bBOXRT, Schemathesis, Dredd, Tcases, and APIFuzzer for the services.

```
python3 run_tool.py {tool_name} {service_name} {time_limit}
```

### Anaylze the result.

You can analyze the result. You provide the automatic reporting script.


```
python3 report.py {port number} {service name}
```

### Stop service

Users can stop service using the following command.

```
python3 stop_service.py {service name}
```

### Result

![res](images/figure_all.png)

1: Evo-White, 2: RESTler, 3: RestTestGen, 4: RESTest, 5: bBOXRT , 6: Schemathesis, 7: Tcases, 8: Dredd, 9: Evo-Black, 10: APIFuzzer, A: Features-Service, B: Languagetool, C: NCS, D: News, E: OCVN, F: ProxyPrint, G: Restcountries, H: Scout-API, I: SCS, J: ERC20-Rest-Service, K: Genome-Nexus, L: Person-Controller, M: Problem-Controller, N: Rest-Study, O: Spring-Batch-Rest, P: Spring-Boot-Sample-App, Q: User-Management, R: CWA-Verification, S: Market, T: Project-Tracking-System. The color of the bar represents the running time - 10 min: ![10min](images/10min.png), 20 min: ![20min](images/20min.png), 30 min: ![30min](images/30min.png), 40 min: ![40min](images/40min.png), 50 min: ![50min](images/50min.png), 60 min: ![1h](images/1h.png), and 24 hr: ![24h](images/24h.png).
