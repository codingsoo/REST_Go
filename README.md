# Automated Test Generation for REST APIs: No Time to Rest Yet

## Getting Started

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

It will take around 18 minutes and 30 seconds.

### Run

You can run the service and tool using run_small.py. The python script runs the service for six minutes.
You can select tool name and port name. Possible tool name can be: evomaster-whitebox, evomaster-blackbox, restler, restest, resttestgen, bboxrt, schemathesis, dredd, tcases, and apifuzzer.
You can pick any free port number for the port name. The port number is for collecting the achieved code coverage.

```
python3 run_small.py {tool_name} {port_number}
```

It will automatically start the service and coverage collecting script. You can check them with the command "tmux ls" and kill them with the command "tmux kill-sess -t {session name}."

### Generate Report

### Check the basic functionality

In this section, we give detailed instructions to check the basic functionality of this artifact.
We show how our tool works with project-tracking-system service.

### How to setup the environment?

We used Google Cloud e2-standard-4 machines running Ubuntu 20.04. Each machine has four 2.2GHz Intel-Xeon processors and 16GB RAM. The major dependencies that we used are Java8, Java11, Python3.8, NodeJS v10.19, and Docker 20.10. We provide a setup script that setups the environment, tools, and services. Please note that we used Ubuntu 20.04 environment.

```
sh setup.sh
```

We have configured the each database for the service using the Docker, and that is automatically done in our script. However, users need to set Private Ethereum network if they want to run ERC20-rest-service.
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

```
python3 run_service.py {service_name} {jacoco_port} {whitebox} # If you want to run blackbox tools, type blackbox instead of whitebox
```

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

In the root directory, you will have a log file and jacoco coverage report file. You can generate a coverage report using jacoco (java -jar org.jacoco.cli-0.8.7-nodeps.jar report ...).

### Current tool setup & configuration

Currently, we used the setup & configuration after reading each tool's manual and paper. We chose recommended options, but not all options are described in most cases, and the best choices depend on the target REST API service, combinations of the options, and the tool's running time. We tried to find the setup for finding their best-performing option, but if the testing tool requires exact request end-point dependency information and the parameter value, we did not follow them since finding them is one of our goal of this comparison.

- EvoMasterWB: We implemented an EvoMasterWB driver to instrument Java/Kotlin code and inject data resources into the database. To write the driver, we checked two videos ([1](https://www.youtube.com/watch?v=3mYxjgnhLEo), [2](https://www.youtube.com/watch?v=ORxZoYw7LnM)) and [a document] (https://github.com/EMResearch/EvoMaster/blob/master/docs/write_driver.md). We made sure that we could set up the source code instrument part, which is their main contribution.
- EvoMasterBB: We referred to [a document](https://github.com/EMResearch/EvoMaster/blob/master/docs/blackbox.md). We kept all options as default because they don't have any recommendations in the document.
- Dredd: From the [Dredd document](https://dredd.org/en/latest/), Dredd needs example values for required parameters. We put default example values such as 123, "abc," and 0.123 to the required parameters if they don't have any example value.
- RESTler: We used the BFS-fast fuzzing mode for the one-hour runs and the random-walk fuzzing mode for the 24-hour run. We turned on all security checkers. Those options are from their paper and their [Github repository](https://github.com/microsoft/restler-fuzzer).
- RestTestGen: This tool has just been released, and it does not have recommended options yet. We used the jar file that the author gave us and used the default setting.
- bBOXRT: We could not find recommended options in their [document](https://git.dei.uc.pt/cnl/bBOXRT) and their paper, so we used default options.
- RESTest: We referred to [a document](https://github.com/isa-group/RESTest). We used the main README example setting as a default setting and provided inter-parameter dependency information for the benchmarks in the format required by the tool.
- Schemathesis: Since the paper used three options and quick checks that require a short time, we tried all three options by rotating each option sequentially: all checkers, negative testing, and default mode. The quick check feature lets us run the tool in ten minutes for all options they used in their paper, so we found we could get the best Schemathesis option if we use all three options by rotating them.
- Tcases: Since this tool only generates the test cases but does not have a feature for sending the request, we made a simple script to send the request using the generated request. We used [Junit](https://junit.org/junit4/) library to send the generated tests' requests. To avoid Java constant pool limit error, we used the -S option to divide into each API path.
- APIFuzzer: In their [document](https://github.com/KissPeter/APIFuzzer), we could not find any recommended options, so default options are used in our experiment.

### Result

![res](images/figure_all.png)

1: Evo-White, 2: RESTler, 3: RestTestGen, 4: RESTest, 5: bBOXRT , 6: Schemathesis, 7: Tcases, 8: Dredd, 9: Evo-Black, 10: APIFuzzer, A: Features-Service, B: Languagetool, C: NCS, D: News, E: OCVN, F: ProxyPrint, G: Restcountries, H: Scout-API, I: SCS, J: ERC20-Rest-Service, K: Genome-Nexus, L: Person-Controller, M: Problem-Controller, N: Rest-Study, O: Spring-Batch-Rest, P: Spring-Boot-Sample-App, Q: User-Management, R: CWA-Verification, S: Market, T: Project-Tracking-System. The color of the bar represents the running time - 10 min: ![10min](images/10min.png), 20 min: ![20min](images/20min.png), 30 min: ![30min](images/30min.png), 40 min: ![40min](images/40min.png), 50 min: ![50min](images/50min.png), 60 min: ![1h](images/1h.png), and 24 hr: ![24h](images/24h.png).
