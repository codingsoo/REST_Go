package io.resttestgen.nominaltester.cli;

import io.resttestgen.nominaltester.helper.SessionClassLoader;
import io.resttestgen.nominaltester.helper.SwaggerCodegenHelper;
import io.resttestgen.nominaltester.helper.exceptions.ClassLoaderNotInitializedException;
import io.resttestgen.nominaltester.helper.exceptions.CodegenParserException;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.testers.DictionaryBasedTester;
import io.resttestgen.nominaltester.testers.Tester;
import io.resttestgen.swaggerschema.SchemaEditor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class App {

    private static final String toolVersion = "19.11.21";
    private static final String helpMessage = "RESTTESTGEN " + toolVersion + "\n\n"
            + "Parameters:\n"
            + "(REQUIRED) --classes <path-of-generated-swagger-client-jar> (es. client-codegen.jar)\n"
            + "(REQUIRED) --swagger <swagger-specification-file> (e.g. /my-rest/petstore.json)\n"
            + "(OPTIONAL) --output output-directory (default output)\n"
            + "(OPTIONAL) --auth auth class\n"
            + "(OPTIONAL) --verbose log-verbosity-level (e.g. 0 for info, 1 for debug, default: 1)\n"
            + "(OPTIONAL) --tester tester-class-to-use (default: DependencyGraphBasedOperationTester)\n"
            + "(OPTIONAL) --dictionary path-to-init-json-file-for-dictionary (e.g. initDictionary.json)\n";


    static Logger logger;

    public static void changeLogDirectory(String newDirectoryForLogs, String logFileName) {
        String logPath = Paths.get(newDirectoryForLogs).resolve(logFileName).toAbsolutePath().toString();
        PatternLayout pattern = PatternLayout.newBuilder().withPattern("%d{yyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n").build();

        Appender appender = FileAppender.newBuilder()
                .withFileName(logPath)
                .withAppend(false)
                .withName("MyFile")
                .withImmediateFlush(true)
                .withLayout(pattern)
                .build();

        Logger logger = LogManager.getRootLogger();
        ((org.apache.logging.log4j.core.Logger) logger).getContext().getRootLogger().addAppender(appender);
        ((org.apache.logging.log4j.core.Logger) logger).getContext().getRootLogger().setLevel(Level.DEBUG);
    }

    public static void main(String[] args) {

        // Check command-line args validity
        checkArgsValidity(args);

        // Get args
        CommandOptions cmd = new CommandOptions(args);
        String generatedClassesPath = cmd.valueOf("--classes");
        String swaggerFilePath = cmd.valueOf("--swagger");
        String authHookClassName = cmd.valueOf("--auth");
        String resetHookClassName = cmd.valueOf("--reset");
        String outputDir = cmd.valueOf("--output");
        String dictionaryPath = cmd.valueOf("--dictionary");
        String testerStrategyClass = cmd.hasOption("--tester") ? cmd.valueOf("--tester") : "DependencyGraphBasedOperationTester";
        testerStrategyClass = "io.resttestgen.nominaltester.testers.operationtestingstrategies." + testerStrategyClass;

        if (outputDir != null) {
            ExAppConfig.outputFolder = outputDir;
        }

        changeLogDirectory(ExAppConfig.outputFolder, "nominal_tester.log");
        logger = LogManager.getLogger(App.class);
        logger.info("cli-param1: swagger-file: {}", swaggerFilePath);
        logger.info("cli-param2: swagger-codegen jar: {}", generatedClassesPath);

        // Read swagger file; extract operations and link the swagger-gen methods
        logger.info("Reading swagger file");
        OpenAPI openAPI = parseSwaggerFile(swaggerFilePath);


        // Use URLClassLoader to load codegen classes
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = SessionClassLoader.createInstance(generatedClassesPath).getUrlClassLoader();
        } catch (MalformedURLException e) {
            logger.error("Cannot use URLClassLoader to load codegen classes", e);
            System.exit(-3);
        }

        Class<?> authHookClass = checkClassValidity(urlClassLoader, authHookClassName);
        Class<?> resetHookClass = checkClassValidity(urlClassLoader, resetHookClassName);
        Class<?> testerClass = checkClassValidity(urlClassLoader, testerStrategyClass);

        // Get operations from swagger
        Map<String, List<OperationInfo>> classOperationsMap = null;
        try {
            logger.info("Reading generated operations from swagger-codegen");
            classOperationsMap = SwaggerCodegenHelper.getOperationsFromSwagger(openAPI);
        } catch (CodegenParserException | ClassLoaderNotInitializedException e) {
            logger.error("Error while parsing swagger operations", e);
            System.exit(-4);
        }

        // Extract OperationInfos
        List<OperationInfo> operations = classOperationsMap.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
        logger.info("Number of operations found: " + operations.size());

        try {
            // Create a new instance of tester given user options
            Constructor<?> constructor = testerClass.getConstructor(OpenAPI.class, Map.class);
            Tester tester = (Tester)constructor.newInstance(openAPI, classOperationsMap);
            logger.info("New tester of class: " + tester.getClass().getSimpleName());

            // Set auth and reset classes if provided
            if (authHookClass != null) tester.setAuthHookClass(authHookClass);
            if (resetHookClass != null) tester.setResetHookClass(resetHookClass);

            // Init the dictionary from file if provided
            if (tester instanceof DictionaryBasedTester && dictionaryPath != null) {
                logger.info("Init the dictionary from file " + dictionaryPath);
                ((DictionaryBasedTester) tester).getResponseDictionary().addFromJSONFile(dictionaryPath);
            }

            // Run the tester
            tester.run();
        } catch (ReflectiveOperationException e) {
            logger.error("Fail while initializing the tester object", e);
            e.printStackTrace();
            System.exit(-6);
        }
    }

    private static Class<?> checkClassValidity(URLClassLoader urlClassLoader, String targetClassName) {
        if (urlClassLoader == null) {
            logger.error("Url class loader is not initialized");
            System.exit(-4);
        }

        if (targetClassName != null) {
            try {
                return urlClassLoader.loadClass(targetClassName);
            } catch (ClassNotFoundException e){
                logger.error("Class " + targetClassName + " not found", e);
                System.exit(-5);
            }
        }

        return null;
    }


    /**
     * Parse swagger file and enforce some properties
     * - No additional properties
     * - No Empty Strings
     *
     * @param swaggerFilePath swagger file specification
     * @return OpenAPI POJO class
     */
    private static OpenAPI parseSwaggerFile(String swaggerFilePath) {
        OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerFilePath);
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);
        try {
            schemaEditor.setDefaultAdditionalPropertiesToAll(false);
            schemaEditor.setDefaultStringMinLength(0);
        } catch (Exception e) {
            logger.warn("Cannot using enforce strict rules on swagger-validator");
            e.printStackTrace();
        }
        return openAPI;
    }

    /**
     * Parse command args and raise an error if not valid
     * @param args Command args
     */
    private static void checkArgsValidity(String[] args) {
        CommandOptions cmd = new CommandOptions(args);

        String generatedClassesPath = cmd.valueOf("--classes");
        String swaggerFilePath = cmd.valueOf("--swagger");
        String logVerbosity = cmd.valueOf("--verbose");
        String dictionaryPath = cmd.valueOf("--dictionary");

        int help = cmd.searchOption("--help");
        int version = cmd.searchOption("--version");

        if (help >= 0 || version >= 0) {
            System.out.println(helpMessage);
            System.exit(0);
        }

        if (generatedClassesPath == null || swaggerFilePath == null) {
            System.err.println("Error -1: Missing required parameters");
            System.err.println(helpMessage);
            System.exit(-1);
        }

        if (!CommandOptions.checkFileExists(generatedClassesPath)){
            System.err.println("Error -2: " + generatedClassesPath + " does not exists");
            System.exit(-2);
        }

        if (dictionaryPath != null && !CommandOptions.checkFileExists(dictionaryPath)){
            System.err.println("Error -2: " + dictionaryPath + " does not exists");
            System.exit(-2);
        }

        if (!CommandOptions.checkFileExists(swaggerFilePath)){
            System.err.println("Error -2: " + swaggerFilePath + " does not exists");
            System.exit(-2);
        }

        if (logVerbosity != null) {
            int verbosityLevel = Integer.parseInt(logVerbosity);
            switch (verbosityLevel){
                case 1:
                    Configurator.setLevel(App.class.toString(), Level.INFO);
                    break;
                case 2:
                    Configurator.setLevel(App.class.toString(), Level.DEBUG);
                    break;
                default:
                    Configurator.setLevel(App.class.toString(), Level.ERROR);
                    break;
            }
        } else {
            Configurator.setLevel(App.class.toString(), Level.INFO);
        }

    }
}
