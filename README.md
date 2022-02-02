## Automated Test Generation for REST APIs: No Time to Rest Yet

### How to setup the environment?

We used Google Cloud e2-standard-4 machines running Ubuntu 20.04. Each machine has four 2.2GHz Intel-Xeon processors and 16GB RAM. The major dependencies that we used are Java8, Java11, Python3.8, NodeJS v10.19, and Docker 20.10. We provide a setup script that setups the environment, tools, and services. Please note that we used Ubuntu 20.04 environment.

```
sh setup.sh
```

We have configured the each database for the service using the Docker, and that is automatically done in our script. However, users need to set Private Ethereum network if they want to run ERC20-rest-service.

```
geth --datadir ethereum init genesis.json
geth --networkid 42 --datadir ethereum --http --http.port 8545 --http.corsdomain "*" --http.api "admin,db,eth,debug,miner,net,shh,txpool,personal,web3" --port 30303 --mine --allow-insecure-unlock console
>> personal.unlockAccount("05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11")
>> miner.start(1) # wait until mine process starts
>> personal.unlockAccount("05f4172fda1cf398fad85ceb60ad9f4180f0ab3a", "11")
```

Now you are ready to run the experiment!

### How to run the service?

You can run the service cwa-verification, erc20-rest-service, features-service, genome-nexus, languagetool, market, ncs, news, ocvn, person-controller, problem-controller, project-tracking-system, proxyporint, rest-study, restcountries, scout-api, scs, spring-batch-rest, spring-boot-sample-app, and user-management through our python script.

```
python run_service.py {service_name} {coverage_collecting_port} {whitebox if you run EvoMasterWB}
```


### How to run the tool?

You can run EvoMasterWB, EvoMasterBB, RESTler, RESTest, RestTestGen, bBOXRT, Schemathesis, Dredd, Tcases, and APIFuzzer for the services.

```
python run_tool.py {tool_name} {service_name} {time_limit}
```

### Current tool setup & configuration

Currently, we used the setup & configuration after reading each tool's manual and paper. We chose recommended options, but not all options are described in most cases, and the best choice depends on the target REST API service and the tool's running time. We tried to find the setup for finding their best-performing option, but if the testing tool requires direct request end-point dependency information and the parameter value, we did not follow them since they are the goal of this comparison.

- Schemathesis: We rotated three options---all checkers, negative testing, % and default mode.
- RESTest: We provided inter-parameter dependency information for the benchmarks in the format required by the tool.
- EvoMaster: We implemented a driver for instrumenting Java/Kotlin code and database injection.
- RESTler: We used the BFS-fast fuzzing mode for the one-hour runs and the random-walk fuzzing mode for the 24-hour run. We turned on all security checkers.
- Dredd: We put default example values such as 123, "abc," and 0.123 to the required parameters.
