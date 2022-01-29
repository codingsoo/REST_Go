package io.resttestgen.launcher.cli;

import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.codegen.v3.cli.SwaggerCodegen;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class App {

    private static final String toolVersion = "20.12";
    private static final String messageHeader = "RestTestGen " + toolVersion + "\n\n";
    private static final String helpMessage = messageHeader
            + "Parameters:\n"
            + "(OPTIONAL) -c executes Swagger Codegen only\n"
            + "(OPTIONAL) -n executes the Nominal Tester only\n"
            + "(OPTIONAL) -e executes the Error Tester only\n"
            + "\tIf no -c, -n or -e are specified, by default all the three components are executed.\n"
            + "(OPTIONAL) --verbose log-verbosity-level (e.g. 0 for info, 1 for debug, default: 1)\n"
            + "(OPTIONAL) --help shows this message";
    private static final Logger logger = LogManager.getLogger(App.class);

    /**
     * Entry point of the command line application.
     * @param args arguments.
     */
    public static void main(String[] args) {

        AppConfig appConfig = new AppConfig();

        // Configure logger to debug level
        // TODO: set verbosity based on the parameter
        Configurator.setRootLevel(Level.DEBUG);

        // Check args
        checkArgsValidity(args, appConfig);

        // Set the working directory
        appConfig.workingDirectory = System.getProperty("user.dir") + File.separator;
        logger.info("RestTestGen started in " + appConfig.workingDirectory);

        // Path of the configuration in single mode
        Path singleModeUserConfigPath = Paths.get(appConfig.workingDirectory, appConfig.configurationFilename);

        if (Files.exists(singleModeUserConfigPath)) {
            // Configuration found. Running in single mode with custom configuration

            try {
                appConfig.importUserConfig(new UserConfig(singleModeUserConfigPath));
                logger.info("Custom configuration found. Running in single mode.");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Cannot import the configuration. Check the syntax.");
                System.exit(-2);
            }
            run(appConfig);
        } else if (Files.exists(Paths.get(appConfig.workingDirectory, appConfig.openApiSpecificationFilename))) {
            // Configuration not found, but specification found. Running in single mode with default configuration

            logger.info("Specification found. Running in single mode");
            run(appConfig);
        } else {
            // Configuration and specification not found. Running in multi mode

            logger.info("Configuration and specification not found in working directory. Entering multi mode.");
            Path dir = Paths.get(appConfig.workingDirectory);
            List<Path> subDirectories = new ArrayList<>();
            try {
                Files.walk(dir, 1)
                        .filter(p -> Files.isDirectory(p) && ! p.equals(dir))
                        .forEach(p -> subDirectories.add(p.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Cannot read the directory subtree.");
                System.exit(-2);
            }
            logger.info("Subdirectories found: " + subDirectories.toString());
            for (Path subDirectory : subDirectories) {

                // Create a copy of the current appConfig for the single execution of the multi mode.
                AppConfig multiModeAppConfig = new AppConfig(appConfig);

                // Update working directory including the subfolder
                multiModeAppConfig.workingDirectory = multiModeAppConfig.workingDirectory + subDirectory.toString()
                        + File.separator;

                Path multiModeUserConfigPath = Paths.get(multiModeAppConfig.workingDirectory,
                        multiModeAppConfig.configurationFilename);
                if (Files.exists(multiModeUserConfigPath)) {
                    // Custom configuration for subdirectory found.

                    try {
                        multiModeAppConfig.importUserConfig(new UserConfig(multiModeUserConfigPath));
                        logger.info("Custom configuration found. Running in the subdirectory " + subDirectory.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Could not import the custom configuration for the subdirectory " +
                                subDirectory.toString() + ". Check the syntax.");
                        System.exit(-2);
                    }
                    run(multiModeAppConfig);
                } else if (Files.exists(Paths.get(multiModeAppConfig.workingDirectory,
                        multiModeAppConfig.openApiSpecificationFilename))) {
                    // Configuration not found, but specification found. Running in single mode with default configuration

                    logger.info("Specification found. Running in the subdirectory " + subDirectory.toString());
                    run(multiModeAppConfig);
                } else {
                    // Specification nor configuration found. Skipping the subdirectory.
                    logger.info("Specification nor configuration found. Skipping subdirectory "
                            + subDirectory.toString());
                }
            }
        }
    }


    /**
     * Parses command arguments.
     * @param args arguments.
     * @return a set of modules to execute. 0: codegen, 1: nominal tester, 2: error tester, 3: security tester.
     */
    private static void checkArgsValidity(String[] args, AppConfig appConfig) {
        CommandOptions cmd = new CommandOptions(args);

        int codegen = cmd.searchOption("-c");
        int nominal = cmd.searchOption("-n");
        int error = cmd.searchOption("-e");
        String verbose = cmd.valueOf("--verbose");
        int help = cmd.searchOption("--help");

        // If help is present, print help message and exit.
        if (help >= 0) {
            System.out.println(helpMessage);
            System.exit(0);
        }

        // Set the execution of modules based on args
        if (codegen >= 0) {
            appConfig.runCodegen = true;
            appConfig.runNominalTester = false;
            appConfig.runErrorTester = false;
        }
        if (nominal >= 0) {
            appConfig.runCodegen = true;
            appConfig.runNominalTester = true;
            appConfig.runErrorTester = false;
        }
        if (error >= 0) {
            appConfig.runCodegen = true;
            appConfig.runNominalTester = true;
            appConfig.runErrorTester = true;
        }
    }


    /**
     * Main procedure of RestTestGen, launching the modules.
     * @param appConfig configuration.
     */
    public static void run(AppConfig appConfig) {

        logger.info("Starting execution.");

        logger.info("Executing SwaggerSchema to check and integrate the provided OpenAPI specification.");
        runSwaggerSchema(appConfig);
        logger.info("SwaggerSchema execution finished.");

        if (appConfig.runCodegen) {
            logger.info("Searching for Swagger Codegen generated test classes");
            if (hasSwaggerCodegenAlreadyBeenExecuted(appConfig)) {
                logger.info("Swagger Codegen output found!");
            } else {
                logger.info("Swagger Codegen output not found! Running Swagger Codegen...");
                runSwaggerCodegen(appConfig);
            }

            // Sleep to prevent conflicts (as learned from Emanuele's run.sh)
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            logger.info("Searching for Swagger Codegen compiled test classes");
            if (haveClassesAlreadyBeenCompiled(appConfig)) {
                logger.info("Compiled classes found!");
            } else {
                logger.info("Compiled classes not found! Running gradle...");
                if (compileGeneratedClasses(appConfig)) {
                    logger.info("Classes compiled successfully");
                } else {
                    logger.error("Gradle compiler process returned a non-0 exit value");
                }
            }
        }

        if (appConfig.runNominalTester) {
            logger.info("Executing Nominal Tester...");
            runNominalTester(appConfig);
            logger.info("Nominal Tester execution completed");
        }

        if (appConfig.runErrorTester) {
            logger.info("Executing Error Tester...");
            runErrorTester(appConfig);
            logger.info("Error Tester execution completed");
        }
    }

    /**
     * Executes the SwaggerSchema module, which produces a new version of the specification with some improvements
     *
     * @param appConfig the configurations
     */
    public static void runSwaggerSchema(AppConfig appConfig) {
        File outputDir = new File(appConfig.workingDirectory + appConfig.outputDirectory);
        if (!outputDir.exists()){
            outputDir.mkdirs();
        }
        String swaggerSchemaParameters = "--swagger " + appConfig.workingDirectory + appConfig.openApiSpecificationFilename + " " +
                "--output " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.editedOpenApiSpecificationFilename + " " +
                "--ambiguous-ids";
        io.resttestgen.swaggerschema.cli.App.main(swaggerSchemaParameters.split(" "));
    }

    /**
     * Checks whether the Swagger Codegen has already been executed.
     *
     * @return true if the Swagged Codegen compiled test classes are present in the intermediate directory
     */
    public static boolean hasSwaggerCodegenAlreadyBeenExecuted(AppConfig appConfig) {
        /* TODO: implement a better heuristic, since at the moment it only checks if a directory exists. */
        return Files.exists(Paths.get(appConfig.outputDirectory + appConfig.codegenOutputDirectory + "src/"));
    }

    /**
     * Executes Swagger Codegen from the JAR file imported as dependency. Parameters for Swagger Codegen are filled
     * with the configuration in the appConfig class. Reads the OpenAPI specification JSON file. Output classes are
     * written in the intermediate folder.
     */
    public static void runSwaggerCodegen(AppConfig appConfig) {
        String swaggerCodegenParameters = "generate -DhideGenerationTimestamp=true " +
                "-i " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.editedOpenApiSpecificationFilename + " " +
                "-l java " +
                "-o " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.codegenOutputDirectory;
        SwaggerCodegen.main(swaggerCodegenParameters.split(" "));
    }

    /**
     * Checks whether the classes generated by Swagger Codegen have already been compiled with gradle.
     *
     * @return true if classes
     */
    public static boolean haveClassesAlreadyBeenCompiled(AppConfig appConfig) {
        return Files.exists(Paths.get(appConfig.workingDirectory + appConfig.outputDirectory
                + appConfig.codegenOutputDirectory + "build/"));
    }

    /**
     * Compiles the classes generated by Swagger Codegen
     * TODO: at the moment only bash is supported.
     *
     * @return true if compilation succeeded, false otherwise
     */
    public static boolean compileGeneratedClasses(AppConfig appConfig) {
        // Copy Gradle wrapper from resources to Codegen path
        ClassLoader classLoader = App.class.getClassLoader();
        final String gradleWrapperFilename = "gradle-wrapper.jar";
        final String gradleWrapperPropertiesFilename = "gradle-wrapper.properties";
        Path wrapperDestination = Paths.get(appConfig.workingDirectory + appConfig.outputDirectory
                + appConfig.codegenOutputDirectory + "gradle/wrapper/" + gradleWrapperFilename);
        Path wrapperPropertiesDestination = Paths.get(appConfig.workingDirectory + appConfig.outputDirectory
                + appConfig.codegenOutputDirectory + "gradle/wrapper/" + gradleWrapperPropertiesFilename);
        try {
            Files.copy(classLoader.getResourceAsStream(gradleWrapperFilename), wrapperDestination,
                    StandardCopyOption.REPLACE_EXISTING);
            Files.copy(classLoader.getResourceAsStream(gradleWrapperPropertiesFilename), wrapperPropertiesDestination,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "cd " + appConfig.workingDirectory + appConfig.outputDirectory
                + appConfig.codegenOutputDirectory + " && bash gradlew build -x test && bash gradlew fatJar");

        try {
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println(output);
                return true;
            } else {
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Executes the Nominal Tester
     */
    public static void runNominalTester(AppConfig appConfig) {
        String nominalTesterParameters = "--classes " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.codegenOutputDirectory + "build/libs/" + appConfig.codegenClassesJarFilename + " " +
                "--swagger " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.editedOpenApiSpecificationFilename + " " +
                "--output " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.nominalTesterOutputDirectory;
        io.resttestgen.nominaltester.cli.App.main(nominalTesterParameters.split(" "));
    }

    /**
     * Executes the Error Tester
     */
    public static void runErrorTester(AppConfig appConfig) {
        String errorTesterParameters = "--service " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.errorTesterOutputDirectory + " " +
                "--reports " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.nominalTesterOutputDirectory + "reports/ " +
                "--swagger " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.editedOpenApiSpecificationFilename + " " +
                "--output " + appConfig.workingDirectory + appConfig.outputDirectory + appConfig.errorTesterOutputDirectory + "reports/";

        try {
            io.resttestgen.errortester.cli.App.main(errorTesterParameters.split(" "));
        } catch (SchemaValidationException e) {
            e.printStackTrace();
        }
    }
}