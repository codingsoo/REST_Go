package pt.uc.dei.rest_api_robustness_tester;

//import net.openhft.compiler.CompilerUtils;
//import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.apache.http.HttpHost;
import pt.uc.dei.rest_api_robustness_tester.AI.HillClimbing;
import pt.uc.dei.rest_api_robustness_tester.AI.InformedSearch;
import pt.uc.dei.rest_api_robustness_tester.faultload.Faultload;
import pt.uc.dei.rest_api_robustness_tester.faultload.FaultloadExecutor;
import pt.uc.dei.rest_api_robustness_tester.faultload.FaultloadExecutorConfig;
import pt.uc.dei.rest_api_robustness_tester.faultload.FaultloadGenerator;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApi;
import pt.uc.dei.rest_api_robustness_tester.specification.RestApiSetup;
import pt.uc.dei.rest_api_robustness_tester.utils.Config;
import pt.uc.dei.rest_api_robustness_tester.utils.XlsxWriter;
import pt.uc.dei.rest_api_robustness_tester.workload.*;


import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;

public class RestApiRobustnessTester {


    private RestApi restAPI = null;
    private Workload workloadResults = null;
    private Workload faultLoadResults = null;
    protected boolean controlerAI = false;
    private LocalWorkloadExecutor workloadExecutor = null;


    private void checkArgs(ArrayList<String> args){
        try
        {
            Map<String, String> argsMap = new LinkedHashMap<>();
            String lastArg = null;
            for(String arg : args)
            {
                if(arg.startsWith("--"))
                {
                    lastArg = arg;
                    argsMap.put(lastArg, "");
                }
                else
                {
                    if(lastArg != null)
                        argsMap.put(lastArg, arg);
                }
            }
            new Config(argsMap);
        }
        catch(Exception e)
        {
            System.out.println("Invalid arguments!");
            System.out.println("Argument syntax: [options...] --api-file <API *.java file> --api-yaml-file <API *.yaml file>");
            System.out.println("Available options:");
            System.out.println("\t--wl-results <workload results *.xlsx file> xlsx file needed as a buffer to save workload requests");
            System.out.println("\t--fl-results <faultload results *.xlsx file> xlsx file to store the faultload results");
            System.out.println("\t--wl-rep <reps> Number of parameter repetitions for workload generation (default is 10)");
            System.out.println("\t--wl-retry <retries> Number of retries for failed workload requests (default is 1)");
            System.out.println("\t--wl-max-time <seconds> Maximum duration of workload execution or 0 for exhausting all requests (default is 0)");
            System.out.println("\t--fl-rep <reps> Maximum number of times a fault can be injected into a request parameter (default is 3)");
            System.out.println("\t--fl-max-time <seconds> Maximum duration of faultload execution or 0 for exhausting all requests (default is 0)");
            System.out.println("\t--out <file name> Print output to an external file rather than to the console");
            System.out.println("\t--keep-failed-wl Keeps the workload requests that result in a non 2xx response");
            System.out.println("\t--proxy-port <port> Run as a fault injector proxy (i.e., only faultload execution) bound to this port");
            System.out.println("\t--conn-timeout <seconds> Timeout value for all connections (default is 60)");
            System.exit(0);
        }

        if(Config.Instance().out != null)
        {
            try
            {
                PrintStream output = new PrintStream(new File(Config.Instance().out));
                System.setOut(output);
                System.setErr(output);
            }
            catch(Exception e)
            {
                System.err.println("Could not redirect output to file " + Config.Instance().out);
            }
        }
    }

    private ArrayList<String> createArrayListofArgs(String pathToApi, String pathToYamlFile, String pathToWLresults,
                                                    String pathToFLresults , String wlRep , String wlRetry , String wlMaxTime,
                                                    String flRep, String flMaxTime , String outFile , boolean keepFailedWl,
                                                    String proxyPort, String connTimeout){
        ArrayList<String> args = new ArrayList<>();
        if(pathToApi != null){
            args.add("--api-file");
            args.add(pathToApi);
        }
        if(pathToYamlFile != null){
            args.add("--api-yaml-file");
            args.add(pathToYamlFile);
        }
        if(pathToWLresults != null){
            args.add("--wl-results");
            args.add(pathToWLresults);
        }
        if(pathToFLresults != null){
            args.add("--fl-results");
            args.add(pathToFLresults);
        }
        if(wlRep != null){
            args.add("--wl-rep");
            args.add(wlRep);
        }
        if(wlRetry != null){
            args.add("--wl-retry");
            args.add(wlRetry);
        }
        if(wlMaxTime != null){
            args.add("--wl-max-time");
            args.add(wlMaxTime);
        }
        if(flRep != null){
            args.add("--fl-rep");
            args.add(flRep);
        }
        if(flMaxTime != null){
            args.add("--fl-max-time ");
            args.add(flMaxTime);
        }
        if(outFile != null){
            args.add("--out");
            args.add(outFile);
        }
        if(keepFailedWl == true){
            args.add("--keep-failed-wl");
        }
        if(proxyPort != null){
            args.add("--proxy-port");
            args.add(proxyPort);
        }
        if(connTimeout != null){
            args.add("--conn-timeout");
            args.add(connTimeout);
        }

        return args;
    }

    /**
     * This method is needed to define the arguments in order for running the tool. <br>
     *
     * Optional settings will run with default values. To personalize optional settings use
     * {@link #initWithOptions(String, String, String, String, String, String, String, String, String, String, boolean, String, String)} method.<br>
     *
     *
     * @param pathToApi  Path to uncompiled Java class the tool uses
     *                  for loading and setting up each API.<br>
     *
     * @param pathToYamlFile  Path to YAML-formatted Swagger/OpenAPI file
     *                       with specifications (metadata, paths, parameters, etc) of the REST API.
     *
     */
    public void initWithDefaultOptions(String pathToApi, String pathToYamlFile)
    {


        ArrayList<String> args = createArrayListofArgs(pathToApi, pathToYamlFile, null,
                null ,  null ,  null ,  null,
                null,  null ,  null ,  false, null,  null);
        checkArgs(args);


    }


    /**
     * This method is needed to define the arguments and options in order for running the tool.<br>
     *
     * [optional param] can have null value.<br>
     *
     * @param pathToApi  Path to uncompiled Java class the tool uses
     *                  for loading and setting up each API. [MANDATORY param]<br>
     *
     * @param pathToYamlFile  Path to YAML-formatted Swagger/OpenAPI file
     *                       with specifications (metadata, paths, parameters, etc) of the REST API. [MANDATORY param]<br>
     *
     * @param pathToWLresults   Path to xlsx file to store the workload results. [optional param]<br>
     *
     * @param pathToFLresults   Path to xlsx file to store the faultload results. [optional param]<br>
     *
     * @param wlRep  Number of parameter repetitions for workload generation (default is 10). [optional param] <br>
     *
     * @param wlRetry  Number of retries for failed workload requests (default is 1). [optional param] <br>
     *
     * @param wlMaxTime   Maximum duration (in seconds) of workload execution or 0
     *              for exhausting all requests (default is 0). [optional param] <br>
     *
     * @param flRep  Maximum number of times a fault can be injected into
     *                  a request parameter (default is 3) (repetitions). [optional param] <br>
     *
     * @param flMaxTime  Maximum duration (in seconds) of faultload execution or 0
     *                  for exhausting all requests (default is 0). [optional param] <br>
     *
     * @param outFile File name for printing output
     *                to an external file rather than to the console. [optional param] <br>
     *
     * @param keepFailedWl   Keeps the workload requests
     *                     that result in a non 2xx response. [optional param] <br>
     *
     * @param proxyPort  Run as a fault injector proxy
     *                  (i.e., only faultload execution) bound to this port. [optional param] <br>
     *
     * @param connTimeout   Timeout value (in seconds) for all connections (default is 60). [optional param] <br>
     */
    public void initWithOptions(String pathToApi, String pathToYamlFile, String pathToWLresults,
                      String pathToFLresults , String wlRep , String wlRetry , String wlMaxTime,
                              String flRep, String flMaxTime , String outFile , boolean keepFailedWl,
                                String proxyPort, String connTimeout)
    {

        ArrayList<String> args = createArrayListofArgs(pathToApi, pathToYamlFile, pathToWLresults,
                 pathToFLresults ,  wlRep ,  wlRetry ,  wlMaxTime,
                 flRep,  flMaxTime ,  outFile ,  keepFailedWl, proxyPort,  connTimeout);
        checkArgs(args);
    }


    /**
     *
     * Method needed to define the arguments in order for running RestApiRobustnessTester tool. <br>
     *
     * This method is recommended to use if the arguments of
     * main method being used are the same as the required to run RestApiRobustnessTester.
     *
     * @param args String array with the arguments needed to run the tool<br>
     *             <br>
     *             Mandatory arguments:
     *             <br>
     *
     *             --api-file &lt;*.java&gt; - Path to uncompiled Java class the tool uses
     *             for loading and setting up each API.<br>
     *
     *             --api-yaml-file &lt;*.yaml&gt; - Path to YAML-formatted Swagger/OpenAPI file
     *             with specifications (metadata, paths, parameters, etc) of the REST API.<br>
     *
     *             <br>
     *             Options:
     *             <br>
     *
     *             --wl-results &lt;*.xlsx&gt; - Path to xlsx file to store the workload results<br>
     *
     *             --fl-results &lt;*.xlsx&gt; - Path to  xlsx file to store the faultload results<br>
     *
     *             --wl-rep &lt;reps&gt; -  Number of parameter repetitions for workload generation (default is 10)<br>
     *
     *             --wl-retry &lt;retries&gt; - Number of retries for failed workload requests (default is 1)<br>
     *
     *             --wl-max-time &lt;seconds&gt; -  Maximum duration (in seconds) of workload execution or 0
     *             for exhausting all requests (default is 0)<br>
     *
     *             --fl-rep &lt;reps&gt; -  Maximum number of times a fault can be injected into
     *             a request parameter (default is 3)<br>
     *
     *             --fl-max-time &lt;seconds&gt; -  Maximum duration  of faultload execution or 0
     *             for exhausting all requests (default is 0)<br>
     *
     *             --out &lt;*.txt&gt; - Print output to an external file rather than to the console<br>
     *
     *             --keep-failed-wl - Keeps the workload requests that result in a non 2xx response<br>
     *
     *             --proxy-port &lt;port&gt; Run as a fault injector proxy
     *             (i.e., only faultload execution) bound to this port<br>
     *
     *             --conn-timeout &lt;seconds&gt; Timeout value for all connections (default is 60)<br>
     *
     */
    public void Init(String[] args)
    {
        ArrayList<String> auxArgs = new ArrayList(Arrays.asList(args));
        this.checkArgs(auxArgs);
    }

    public void runAI() throws Exception {
        this.controlerAI = true;
        this.generateAndRunWorkload();
        XlsxWriter workloadWriter = new XlsxWriter(new File(Config.Instance().workloadResultsFile));
        HillClimbing hc = new HillClimbing(this.workloadExecutor);
        hc.run();
        workloadWriter.Write(restAPI.Name());
    }

    public void runIS(int runNumber) throws Exception {
        this.controlerAI = true;
        this.generateAndRunWorkload();
        XlsxWriter workloadWriter = new XlsxWriter(new File(Config.Instance().workloadResultsFile));
        InformedSearch is = new InformedSearch(this.workloadExecutor);
        is.run(runNumber);
        workloadWriter.Write(restAPI.Name());
    }

    /**
     * Valid requests (i.e., correct according to the specification) are generated and sent to the Rest service.<br>
     *
     * A configuration must be defined before calling {@link #generateAndRunWorkload()}.<br>
     *
     * To specify a configuration use {@link #initWithDefaultOptions(String, String)}
     * or {@link #initWithOptions(String, String, String, String, String, String, String, String, String, String, boolean, String, String)} methods.<br>
     *
     * @return Workload object with the workload generated and executed to the API.
     * @throws Exception
     */
    public Workload generateAndRunWorkload() throws Exception {


        this.loadRestAPI();

        // most likely wont ever be null
        if(this.restAPI == null)
            throw new Exception("Error occurred while getting < API *.java file > ");


        this.checkSecuritySchemas(restAPI);
        Workload validWorkload = null;

        if(Config.Instance().proxyPort == Config.UNKNOWN_PORT) {

            WorkloadGeneratorConfig generatorConfig = new WorkloadGeneratorConfig();
            generatorConfig.RepetitionsPerParameter(Config.Instance().WL_REP);

            WorkloadGenerator workloadGenerator = new WorkloadGenerator(restAPI, generatorConfig);
            Workload workload = workloadGenerator.Generate();

            System.out.println("Workload requests generated: " + workload.WorkloadRequests().size());

            XlsxWriter workloadWriter = new XlsxWriter(new File(Config.Instance().workloadResultsFile));

            WorkloadExecutorConfig workloadConfig = new WorkloadExecutorConfig();
            workloadConfig.MaxRetriesOnFail(Config.Instance().WL_RETRY).
                    ExecuteRandomly().
                    WriteTo(workloadWriter);
            if (Config.Instance().WL_MAX_TIME <= 0)
                workloadConfig.StopWhenAllDone();
            else
                workloadConfig.StopWhenTimeEnds(Config.Instance().WL_MAX_TIME * 1000);
            if (Config.Instance().keepFailedWorkload)
                workloadConfig.KeepFailedRequests();
            else
                workloadConfig.DiscardFailedRequests();

            System.out.println("***************** Workload started *****************");
            if (Config.Instance().WL_MAX_TIME <= 0)
                System.out.println("Stop when workload is exhausted");
            else
                System.out.println("Stop when " + Config.Instance().WL_MAX_TIME + " seconds pass");

            LocalWorkloadExecutor workloadExecutor = new LocalWorkloadExecutor(workload, restAPI, workloadConfig);

            //FIXME: temporarily skipping workload
            // it was commented "workloadExecutor.Execute();"
            if(!this.controlerAI){
                workloadExecutor.Execute();
                this.workloadResults = workloadExecutor.GetResults();
                validWorkload = workload;  //workloadExecutor.GetResults();
                workloadWriter.Write(restAPI.Name());
                System.out.println("***************** Workload finished *****************");
            }
            else{
                this.workloadExecutor = workloadExecutor;
            }

        }

        return validWorkload;
    }

    /**
     * Faulty requests are created by injecting a single fault in each request (e.g., a field is removed from a JSON document).
     * The faulty requests are sent to the service in an attempt to trigger erroneous behaviors.
     *
     * A configuration must be defined before calling {@link #generateAndRunWorkloadPlusFaultLoad()}.<br>
     *
     * To specify a configuration use {@link #initWithDefaultOptions(String, String)}
     * or {@link #initWithOptions(String, String, String, String, String, String, String, String, String, String, boolean, String, String)} methods.<br>
     *
     * @return Workload object with the fault results injected to the API.
     * @throws Exception
     */
    public Workload generateAndRunWorkloadPlusFaultLoad() throws Exception {


        Workload validWorkload = generateAndRunWorkload();


        if(Config.Instance().proxyPort == Config.UNKNOWN_PORT) {
            FaultloadGenerator faultloadGenerator = new FaultloadGenerator();
            Faultload faultload = faultloadGenerator.Generate();

            XlsxWriter faultloadWriter = new XlsxWriter(new File(Config.Instance().faultloadResultsFile));

            FaultloadExecutorConfig faultloadConfig = new FaultloadExecutorConfig();
            faultloadConfig.MaxInjectionsPerFault(Config.Instance().FL_REP).
                    WriteTo(faultloadWriter);

            FaultloadExecutor faultloadExecutor = new FaultloadExecutor(faultload, restAPI, faultloadConfig);

            WorkloadExecutorConfig workloadConfig = new WorkloadExecutorConfig();
            workloadConfig.ExecuteRandomly().
                    Hook(faultloadExecutor).
                    WriteTo(faultloadWriter);
            if (Config.Instance().FL_MAX_TIME <= 0)
                workloadConfig.StopWhenAllDone();
            else
                workloadConfig.StopWhenTimeEnds(Config.Instance().FL_MAX_TIME * 1000);

            System.out.println("Workload requests to use for fault injection: " + validWorkload.WorkloadRequests().size());
            System.out.println("Total injectable faults: " + faultloadExecutor.FaultsLeft());
            Map<String, Integer> faultsPerOperation = faultloadExecutor.FaultsLeftPerOperation();
            for (String op : faultsPerOperation.keySet())
                System.out.println("\t" + op + ": " + faultsPerOperation.get(op) + " faults");

            System.out.println("***************** Workload + Faultload started *****************");
            if (Config.Instance().FL_MAX_TIME <= 0)
                System.out.println("Stop when all faults are exhausted");
            else
                System.out.println("Stop when " + Config.Instance().FL_MAX_TIME + " seconds pass");

            LocalWorkloadExecutor workloadExecutor = new LocalWorkloadExecutor(validWorkload, restAPI, workloadConfig);
            workloadExecutor.Execute();

            this.faultLoadResults = workloadExecutor.GetResults();

            faultloadWriter.Write(restAPI.Name());

            System.out.println("***************** Workload + Faultload finished *****************");

        }
        return validWorkload;
    }


    /**
     * Operate as a fault injection proxy between client and server, bound to {@code portNumber} port.<br>
     *
     * A configuration must be defined before calling {@link #generateAndRunWorkloadPlusFaultLoad()}.<br>
     *
     * To specify a configuration use {@link #initWithDefaultOptions(String, String)}
     * or {@link #initWithOptions(String, String, String, String, String, String, String, String, String, String, boolean, String, String)} methods.<br>
     *
     * @param portNumber Value to run as a fault injector proxy bound to this port number
     * @throws Exception
     */
    public void runProxy(int portNumber) throws Exception {

        this.loadRestAPI();

        if(portNumber > 0)
            Config.Instance().proxyPort = portNumber;
        if(Config.Instance().proxyPort == Config.UNKNOWN_PORT && portNumber <= 0)
            throw new Exception("Port number is needed and must be greater than 0");

        // most likely wont ever be null
        if(this.restAPI == null)
            throw new Exception("Error occurred while getting < API *.java file > ");

        this.checkSecuritySchemas(restAPI);

        if(Config.Instance().proxyPort != Config.UNKNOWN_PORT) {

            System.out.println("Starting proxy fault injector at http://localhost:" + Config.Instance().proxyPort);
            HttpHost proxyHost = new HttpHost("localhost", Config.Instance().proxyPort, "http");

            FaultloadGenerator faultloadGenerator = new FaultloadGenerator();
            Faultload faultload = faultloadGenerator.Generate();

            XlsxWriter faultloadWriter = new XlsxWriter(new File(Config.Instance().faultloadResultsFile));

            FaultloadExecutorConfig faultloadConfig = new FaultloadExecutorConfig();
            faultloadConfig.MaxInjectionsPerFault(Config.Instance().FL_REP).
                    WriteTo(faultloadWriter);

            FaultloadExecutor faultloadExecutor = new FaultloadExecutor(faultload, restAPI, faultloadConfig);

            WorkloadExecutorConfig workloadConfig = new WorkloadExecutorConfig();
            workloadConfig.ExecuteRandomly().
                    ProxyHost(proxyHost).
                    Hook(faultloadExecutor).
                    WriteTo(faultloadWriter);
            if(Config.Instance().FL_MAX_TIME <= 0)
                workloadConfig.StopWhenAllDone();
            else
                workloadConfig.StopWhenTimeEnds(Config.Instance().FL_MAX_TIME * 1000);

            System.out.println("Total injectable faults: " + faultloadExecutor.FaultsLeft());
            Map<String, Integer> faultsPerOperation = faultloadExecutor.FaultsLeftPerOperation();
            for(String op : faultsPerOperation.keySet())
                System.out.println("\t" + op + ": " + faultsPerOperation.get(op) + " faults");

            System.out.println("***************** Proxy faultload started *****************");
            if(Config.Instance().FL_MAX_TIME <= 0)
                System.out.println("Stop when all faults are exhausted");
            else
                System.out.println("Stop when " + Config.Instance().FL_MAX_TIME + " seconds pass");

            RemoteWorkloadExecutor workloadExecutor = new RemoteWorkloadExecutor(restAPI, workloadConfig);
            workloadExecutor.Execute();

            faultloadWriter.Write(restAPI.Name());

            System.out.println("***************** Proxy faultload finished *****************");
        }
    }

    /**
     * Gets the results from the HTTP requests that were generated and executed by the Workload
     * from generateAndRunWorkload() method to the Rest API.<br>
     *
     * If called before {@link #generateAndRunWorkload()}  method
     * then the return will be a null WorkLoad object.<br>
     *
     * @return Workload object with the results obtained.
     * @throws Exception
     */
    public Workload getWorkloadResults() throws Exception {

        // maybe don't need to throw an exception, just a print telling generateAndRunWorkload() method was not called first
        /*if(this.workloadResults == null)
            throw new Exception("Must run generateAndRunWorkload() method first before getting workload Results");*/

        return this.workloadResults;
    }


    /**
     * Gets the results from the faulty HTTP requests generated and executed by the faultload (Workload with faults)
     * from {@link #generateAndRunWorkloadPlusFaultLoad()} method to the Rest API.<br>
     *
     * If called before {@link #generateAndRunWorkloadPlusFaultLoad()}  method
     * then the return will be a null WorkLoad object.<br>
     *
     * @return Workload object with the results obtained after the faults injection to the API.
     * @throws Exception
     */
    public Workload getFaultLoadResults() throws Exception {

        // maybe don't need to throw an exception, just a print telling generateAndRunWorkloadPlusFaultLoad() method was not called first
        /*if(this.faultLoadResults == null)
            throw new Exception("Must run generateAndRunWorkloadPlusFaultLoad() method first before getting faultLoad Results");*/

        return this.faultLoadResults;
    }


    private void loadRestAPI() throws Exception {

        if(!Config.IsInitialized())
            throw new Exception("A Config must be defined before running");

        String apiSetupClassName = new File(Config.Instance().apiClassFile.replace(".java", "")).getName();

        CompilerTest c = new CompilerTest(Config.Instance().apiClassFile, apiSetupClassName);
        RestApiSetup restApiSetup = c.compileClass();
        if(restApiSetup == null){
            throw new Exception("Some unknown error while getting <API *.java file> has occurred!");
        }

        RestApi restAPI = restApiSetup.Load(Config.Instance().apiYamlFile);
        restAPI.Specification().Validate();
        System.out.println(restAPI.Specification().apiNameVersion);

        this.restAPI = restAPI;
    }

    private void checkSecuritySchemas(RestApi restAPI){
        if(!restAPI.Specification().securitySchemes.isEmpty())
        {
            System.out.println("Security schemes:");
            for (String s : restAPI.Specification().securitySchemes.keySet())
                System.out.println("\t" + s + ": " + restAPI.Specification().securitySchemes.get(s).type);
        }
        else
            System.out.println("No security schemes defined");
    }


    public void Run() throws Exception
    {

        if(!Config.IsInitialized())
            throw new Exception("A Config must be defined before running");

        if(Config.Instance().proxyPort == Config.UNKNOWN_PORT)
        {
            this.generateAndRunWorkloadPlusFaultLoad();
        }
        else
        {
            // uses Config.Instance().proxyPort value. input number only needed if using method in a diferent project
            // otherwise Config object will be created first without the need to specify the port number again
            this.runProxy(0);
        }
    }
}
