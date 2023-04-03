# bBOXRT

**bBOXRT** is a black BOX tool for Robustness Testing of REST services.


## Overview


**bBOXRT** uses a service description document as input to generate a set of invalid inputs (e.g., empty and boundary values, strings in special formats, malicious values) that are set to the service in combination with valid parameters.  
The tool can also operate as a fault injection proxy between client and s	erver (i.e., without requiring information regarding the service interface).



## How to run bBOXRT

You can run the program as a **Maven project** (mvn clean install), with an **IDE** or even export to a **runnable Jar**.

### Argument syntax 

	
	[options...] --api-file <API *.java file> --api-yaml-file <API *.yaml file> 
	
	
#### [Mandatory options]:

These options are mandatory to run the tool.  

option  | description
------------- | -------------
`--api-file`  | **Path** **to** uncompiled Java class **file** the tool uses for loading and setting up each API specification.
`--api-yaml-file` | **Path** **to** YAML-formatted Swagger/OpenAPI **file** with specifications (metadata, paths, parameters, etc) of the REST API 



#### [options]:

option  | description
------------- | -------------
`--wl-results`  | path to xlsx file to store the workload results
`--fl-results`  | path to xlsx file to store the faultload results
`--wl-rep <reps>`  | Number of parameter repetitions for workload generation (default is 10)
`--wl-retry <retries>`  | Number of retries for failed workload requests (default is 1)
`--wl-max-time <seconds>`  | Maximum duration of workload execution or 0 for exhausting all requests (default is 0)
`--fl-rep <reps>`  | Maximum number of times a fault can be injected into a request parameter (default is 3)
`--fl-max-time <seconds>`  | Maximum duration of faultload execution or 0 for exhausting all requests (default is 0)
`--out <file name>` | Print output to an external file rather than to the console
`--keep-failed-wl` | Keeps the workload requests that result in a non 2xx response
`--proxy-port <port>` | Run as a fault injector proxy (i.e., only faultload execution) bound to this port
`--conn-timeout <seconds>` | Timeout value for all connections (default is 60)


### First steps:  



Some notes you need to take care before running in the IDE or exported Jar, these notes are related to the **argument syntax**, for better understanding check out the **documented syntax** **[here](#argument-syntax)**:

* For **API *.java** and **API *.yaml** files, bBOXRT tool has **50** API classes available in `/2020-access/Tool/API Specifications` folder. For this example we will **use** **Spotify.java** and its respectitive **yaml file**, they are available in `2020-access/Tool/API Specifications/Public tier 1/Spotify/` folder.


### Run without IDE: 
* [Run as a Maven Project](#run-as-a-maven-project)


### Run with IDE:
* [Run in Eclipse IDE](#run-project-in-eclipse-ide)
* [Run in IntelliJ IDEA](#run-project-in-intellij-idea)
* [Run in Apache Netbeans IDE](#run-project-in-apache-netbeans-ide)
* [Export as a runnable Jar in Eclipse IDE](#export-as-a-runnable-jar-in-eclipse-ide)
* [Export as a runnable Jar in IntelliJ IDEA](#export-as-a-runnable-jar-in-intellij-idea)


### Run as a Maven Project

If you haven't maven installed in your computer then check Apache Maven installation documentation **[here](https://maven.apache.org/install.html)**.

In your terminal execute the following commands:

* `cd /path/to/2020-access/Tool`

* `mvn clean install`

* `cd target/`

* **Run** something like this if you are following the example: 
 	- `java -jar REST_API_Robustness_Tester-1.0.jar --api-file "../API Specifications/Public tier 1/Spotify/Spotify.java" --api-yaml-file "../API Specifications/Public tier 1/Spotify/Spotify.yaml"`
 	- If not you need to define the path to your fitting, should look something like this (according to the [argument syntax](#argument-syntax)): 
 		- `java -jar REST_API_Robustness_Tester-1.0.jar --api-file "/path/to/APIfile.java" --api-yaml-file "/path/to/APIyamlFILE.yaml"` 



### Run project in [Eclipse IDE](https://www.eclipse.org/ide/ "Title") 


* **Go to** `File > Open Projects from File System...`  
 
* In `Import source`: 
	- **Find** the directory where the unzip folder `2020-access` is and then **select** the sub-folder `Tool`. After that just press the `finish` button.


* **Right click the project `Tool`** in Project Explorer:  
	- `Run as` > `Run configurations...`

	
	-  Select tab `Arguments` and in the text box `Program arguments:` the argument syntax should look like this: 
		- `--api-file "API Specifications/Public tier 1/Spotify/Spotify.java" --api-yaml-file "API Specifications/Public tier 1/Spotify/Spotify.yaml"`

The next image represents the configuration, after that press `apply` button and then the `close` button.

![eclipseConfiguration](https://i.ibb.co/2nKj7Ym/Captura-de-ecra-2021-05-23-a-s-16-14-39.png)

* **Right click** class `Main` in `pt.uc.dei.rest_api_robustness_tester` package. 


* `Run as > Java Application`

#### You must now have the program running!


### Run project in [IntelliJ IDEA](https://www.jetbrains.com/idea/ "Title")


* **Go to** `File > Open`  
* **Go to** the directory where the unzip folder `2020-access` is and **select** the sub-folder `Tool`. After that just press the open button.


* **Go to** `Run > Edit Configurations`
* **Press** `+` button
* **Select** `Aplication` template
* In `Program Arguments` text box the argument syntax should look like this: 
 	- `--api-file "API Specifications/Public tier 1/Spotify/Spotify.java" --api-yaml-file "API Specifications/Public tier 1/Spotify/Spotify.yaml"`

The next image represents the configuration, after that press `apply` button and then the `ok` button.

![intelliJ_configuration](https://i.ibb.co/0CqWdcN/Captura-de-ecra-2021-05-23-a-s-01-03-14.png)

* **Right click** in class `Main` in `pt.uc.dei.rest_api_robustness_tester` package. 

* `Run 'Main.main()'`

#### You must now have the program running!


### Run project in [Apache Netbeans IDE](https://netbeans.apache.org/ "Title")


* **Go to** `File > Open Project...`  
* **Go to** the directory where the unzip folder `2020-access` is and **select** the sub-folder `Tool`. After that just press the open button.

* **Go to** `File > Project Properties`.
* **Select** `Run` category.
* In `Main Class:` select `pt.uc.dei.rest_api_robustness_tester.Main`
* In the `Arguments` text box the argument syntax should look something like this: 
	- `--api-file "API Specifications/Public tier 1/Spotify/Spotify.java" --api-yaml-file "API Specifications/Public tier 1/Spotify/Spotify.yaml"`
* In `Working Directory` select the `Tool`  folder.

The next image represents the configuration, after that press the `ok` button.

![NetBeansConfiguration](https://i.ibb.co/2ZCqPcV/Captura-de-ecra-2021-05-23-a-s-16-18-42.png)

* `Run Project`

#### You must now have the program running!


### Export as a runnable Jar in [Eclipse IDE](https://www.eclipse.org/ide/ "Title") 

* **Go to** `File > Open Projects from File System...`  
 
* In `Import source`: 
	- **Find** the directory where the unzip folder `2020-access` is and then **select** the sub-folder `Tool`. After that just press the `finish` button.

* **Right click the project `Tool`** in Project Explorer:  
	- `Export` > `Runnable JAR file`
	- In `Launch configuration:` **select** `Main - Tool`
	- Choose the folder as destination of the exported JAR file.
	- Select the option `Extract required libraries into generated JAR`
	- The next image represents the settings menu to export, after that press the `finish` button.

`Note:` If the **Main** class of the project doesn't appear in "Launch configuration:" then try to run the project as a java application. This way eclipse workspace will be updated and Main class will be available to choose in Runnable JAR file Specification.

![eclipseJarExport](https://i.ibb.co/wsyw821/Captura-de-ecra-2021-05-23-a-s-16-12-59.png)


* By now you must have the runnable JAR ready to run!

* If you did export the **JAR** in the `Tool` sub-folder then open the terminal in this sub-folder and run something like this: 
	- `java -jar bBOXRT.jar --api-file "API Specifications/Public tier 1/Spotify/Spotify.java" --api-yaml-file "API Specifications/Public tier 1/Spotify/Spotify.yaml"`



### Export as a runnable Jar in [IntelliJ IDEA](https://www.jetbrains.com/idea/ "Title") 

* **Go to** `File > Open`  
* **Go to** the directory where the unzip folder `2020-access` is and **select** the sub-folder `Tool`. After that just press the open button.


* **Go to** `File > Project Structure...`
	- **Go to** `Artifacts` and click the `+` button
	- **Select** `Jar` > `From modules with dependencies`
	- In `Main Class:` **choose** `pt.uc.dei.rest_api_robustness_tester.Main` 
	- In `JAR files from libraries` **choose** `extract to the target JAR`
	- In `Directory for META-INF/MANIFEST.MF` **choose** `/2020-access/Tool/src/main/resources` folder and then press `ok` button

![intellijMenu1](https://i.ibb.co/FWYgjHW/Captura-de-ecra-2021-05-23-a-s-01-19-08.png)

* After all that press `Apply` and then the `ok` button

![intellijMenu2](https://i.ibb.co/Cm3SbPC/Captura-de-ecra-2021-05-29-a-s-14-47-43.png)

* **Go to** `Build > Build Artifacts...`
	- `Tool:jar` > `Build`

#### By now you must have the runnable JAR ready to run, it must be in the `2020-access/Tool/out/artifacts/Tool_jar/` folder!



## How to run in Proxy mode

The tool's Proxy is a good way to target specific services of the API. Maybe to exploit an certain URI. It is recommended to generate a workload and run it to get a full scope of the available services present in the API, and then choose and target manually with the tool's proxy.   

### Run in Proxy mode

After setting up the IDE it is needed one more program, in this example [POSTMAN](https://www.postman.com/) was the choosen one. The tool makes the proxy available in a local location like so: `http://localhost:8080`. In Postman is easy to specify the local proxy and send the normal request to test the REST API.

Following the [argument syntax](#argument-syntax) `--proxy-port` option needs to be specify.	The following arguments serve as an example:

`--proxy-port 8080 --api-file "API Specifications/Public tier 2/Ably/Ably.java" --api-yaml-file "API Specifications/Public tier 2/Ably/Ably.yaml"`

Now running the tool should appear in Proxy mode, **waiting for API resquests** to be made in order to apply faults in each request.

### Make requests to the Proxy server

This part is only [POSTMAN](https://www.postman.com/), as mention before running a workload is a good ideia. This way the tool prints every URIs, headers and methods (POST, GET, PUT, ...) needed to make the requests.

We need to specify the location of the Proxy server that it is running, so for that follow these steps:

* Go to `Preferences`/`Settings` 
* Select tab `Proxy`
* Make sure it looks like the next image:

![proxyPostman](https://i.ibb.co/Rz48J83/Captura-de-ecra-2021-05-22-a-s-11-48-48.png)

For this example Ably's API was used. After generate and running the workload a random request was picked to use in the proxy server, the following image represents that request printed in the workload tool (this request was also available in the xlsx file): 

<a name="requestProxyImage"></a>![requestProxy](https://i.ibb.co/xHkzKPD/Captura-de-ecra-2021-05-22-a-s-20-37-32.png) 

The `Request:` section tells that was use a **POST method** with the following **endpoint** `/push/publish`, the only **parameter** was used to get a response in *MessagePack* format (`format=msgpack`).

The **header X-Ably-Version** is specific to the API being use for this example. The **header Authorization** is needed because it is the API key to make the request into the REST API server. Next to the Authorization header is the **request body** (content).

So by now it is possible to target this service in POSTMAN, the following image represents the request to the tool's Proxy server:

![requestProxyPostman](https://i.ibb.co/5x4Q2jQ/Captura-de-ecra-2021-05-22-a-s-20-39-22.png)
The URL `http://localhost:8080` represents the Proxy server and the `Host` header indicates the target host which is the API server. The rest of the headers are equal to the ones present in the previous [image](#requestProxyImage).

The **request body** is represented in the next image:

![requestProxy](https://i.ibb.co/sQLJ6r1/Captura-de-ecra-2021-05-22-a-s-20-39-33.png)

#### Now just send a request to the Proxy Server!



