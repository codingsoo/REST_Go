# RestTestGen
Automated black-box testing of RESTful APIs.

---

### Table of contens
1. [What is RestTestGen](#resttestgen)
2. [Build instructions](#build)
3. [Running RestTestGen](#run)
4. [Configuration](#config)
5. [Execution output](#output)
6. [About](#about)

---

## <a name="resttestgen"></a> What is RestTestGen
RestTestGen is a research tool for automated black-box testing of RESTful APIs. The [ICTS2020 research paper](https://profs.scienze.univr.it/~ceccato/papers/2020/icst2020api.pdf) presents RestTestGen in detail. Following, some basic notions on RestTestGen.
### How does it work (briefly)
RestTestGen reads the OpenAPI specification of a REST API service and generates a series of test cases (nominal and error test cases). Nominal test cases are meant to test the REST service under normal operation, while error test cases are meant to stress the REST service providing wrong inputs within the requests and discover unhandled exceptions or misbehavior of the service.

The sequence of the operations to test is intelligently computed using a dependency graph. Operations that produce useful inputs for other operations, are executed first.

RestTestGen produces as output several reports in JSON format and executable JUnit test cases reproducing the executed test cases.

### Modules
RestTestGen is organized in modules. The `Launcher` module is in charge of launching the others modules, satisfying the execution dependencies among the modules (e.g. the `ErrorTester` module can be executed only after the execution of the `NominalTester` module).

When all the modules are set for launching (the case of a standard execution, without providing parameters), the main routine is the following. First, the `Launcher` runs a customized version of Swagger Codegen which produces some Java classes to be used as clients for the REST service. Immediately after, those classes are compiled. The `NominalTester` module is launched, whom before executing the nominal test cases, uses the `Swagger2DepGraph` module to determine the best sequence of the operations to test. Finally, the `ErrorTester` module executes the error test cases.

Other modules, such as `RequestBuilder` and `SwaggerSchema`, are used here and there in the project.

## <a name="build"></a> Build instructions
*Before building RestTestGen yourself, you might be interested in the pre-compiled and packed JAR executable. Visit the release section of this repository.*

RestTestGen is written in Java and its dependencies are managed with Gradle 6. The RestTestGen module is composed by several sub-modules. The entry point is the `main` method of the `io.resttestgen.launcher.cli.App` class, in the `Launcher` module. The `Launcher` module indeed has a Gradle task named `jar` that compiles and packages both RestTestGen and its dependencies.

Building RestTestGen should be fairly straightforward.

1. Clone this repository with:

```bash
git clone https://github.com/SeUniVr/RestTestGen.git
```

2. Switch to the project folder

```bash
cd RestTestGen
```

3. The gradle wrapper needs the execute permission

```bash
chmod +x ./gradlew
```

4. Build and package RestTestGen with:

```bash
./gradlew jar
```

The `RestTestGen.jar` file will be written in the root directory of the project.

*Tested with Gradle 6.7. If you use the gradle wrapper (as suggested) you can just ignore this.*

## <a name="run"></a> Running RestTestGen
RestTestGen uses the working directory as file system entry point. It requires the working directory to contain the OpenAPI specification file with the name `openapi_specification.json` and an optional configuration file `resttestgen_config.json` to override the default configuration, if required (See [configuration section](#config)).

To start RestTestGen, move with your terminal to the target directory and type:
```
java -jar /path/to/RestTestGen.jar
```
RestTestGen reads the OpenAPI specification file in the working directory and starts the testing with following steps/modules:
- `-c` / Codegen: generates the API client using our customized version of Swagger Codegen
- `-n` / Nominal Tester: executes nominal test cases in the sequence determined by the dependency graph (requires a previous execution of Codegen)
- `-e` / Error Tester: executes the error test cases (requires a previous execution of both Codegen and the Nominal Tester)

To run a limited number of the steps/modules above, just provide the corresponding parameter when launching RestTestGen. E.g., to run the Nominal Tester only type:
```
java -jar /path/to/RestTestGen.jar -n
```
RestTestGen is aware of the execution dependencies among the steps/modules and will take care of the global execution. For example, if the user starts only the Nominal Tester (as shown above) and the Codegen was not previously executed, RestTestGen will launch both the Codegen and the Nominal Tester.

The output of RestTestGen will be stored into the `output/` subdirectory of the working directory.
### Multi mode
If you are interested in testing more REST services at once, RestTestGen provides the multi mode for this purpose. You just need to set up several directories belonging to the same parent directory, each one containing the OpenAPI specification of a REST service (and the optional configuration file) and run RestTestGen in the parent directory. RestTestGen will iterate through the child directories, testing the REST services.

## <a name="config"></a> Configuration
It is possible to customize some settings of RestTestGen, providing a configuration file with name `resttestgen_config.json` in the working directory.
### Available settings
- `openapi_specification_filename`: the name of the input OpenAPI specification file;
- `output_directory`: the directory where RestTestGen writes the output
- `codegen_output_directory`: subdirectory where the source code generated by Swagger CodeGen is put (and compiled)
- `nominal_tester_output_directory`: subdirectory where the Nominal Tester writes its output
- `error_tester_output_directory`: subdirectory where the Error Tester writes its output

*An upcoming release will add more customizable settings.*
### Default configuration
When RestTestGen is launched without providing a configuration file, the following default configuration is used:
```
{
  "openapi_specification_filename": "openapi_specification.json",
  "output_directory": "output/",
  "codegen_output_directory": "codegen/",
  "nominal_tester_output_directory": "nominal_tester/",
  "error_tester_output_directory": "error_tester/"
}
```
## <a name="output"></a> Execution output
The current execution output is a series of JSON files with execution traces and information on the outcome of the tests, along with the executed test cases in JUnit. Variable names in the JSON file are fairly self-explanatory.

Further documentation about the outputs of RestTestGen is coming with the next release of this readme.

## <a name="about"></a> About
RestTestGen is currently maintained by Davide Corradini, PhD student at University of Verona (Italy). Contacts: *davide[dot]corradini[at]univr[dot]it*, or @davidecorradini on GitHub.