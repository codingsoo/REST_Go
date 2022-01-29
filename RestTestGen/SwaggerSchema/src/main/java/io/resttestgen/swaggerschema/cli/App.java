package io.resttestgen.swaggerschema.cli;

import io.resttestgen.swaggerschema.SchemaEditor;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class App {

    private static final String toolVersion = "19.3.21";
    private static final String helpMessage = "SwaggerEditor" + toolVersion + "\n\n"
            + "Required parameters:\n"
            + "--swagger <swagger-specification-file> (es. /my-rest/petstore.json)\n"
            + "--output <swagger-specification-file-output> (es. /my-rest/output.json)\n"
            + "--verbose log-verbosity-level (es. 0 for info, 1 for debug)\n"
            + "\n"
            + "Then add the operation you want to apply:"
            + "--strict (to add stringMinLength = 0 and additionalProperties = false)\n"
            + "--ambiguous-ids (to try to resolve ambiguous Ids)\n";


    static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {

        // Check command-line args validity
        checkArgsValidity(args);

        // Get args
        CommandOptions cmd = new CommandOptions(args);
        String outputFilePath = cmd.valueOf("--output");
        String swaggerFilePath = cmd.valueOf("--swagger");

        logger.info("cli-param1: swagger-file: {}", swaggerFilePath);
        logger.info("cli-param2: output-file: {}", outputFilePath);

        // Read swagger file; extract operations and link the swagger-gen methods
        logger.info("Reading swagger file");
        OpenAPI openAPI = new OpenAPIV3Parser().read(swaggerFilePath);

        // Operations on SchemaEditor
        SchemaEditor schemaEditor = new SchemaEditor(openAPI);

        logger.info("Editing swagger file");
        try {
            int strictOptionExists = cmd.searchOption("--strict");
            if (strictOptionExists >= 0) {
                schemaEditor.setDefaultStringMinLength(0);
                schemaEditor.setDefaultAdditionalPropertiesToAll(false);
            }
            int ambiguousIdsOptionExists = cmd.searchOption("--ambiguous-ids");
            if (ambiguousIdsOptionExists >= 0) {
                schemaEditor.resolveAmbiguousIds();
            }
        } catch (Exception e){
            logger.error("Error during set of operations", e);
            System.exit(-3);
        }

        schemaEditor.fixBasePaths();

        String jsonSchema = SchemaEditor.toJSONSchema(schemaEditor.getOpenAPI());

        try {
            Files.write(Paths.get(outputFilePath), jsonSchema.getBytes());
            logger.info("Wrote edited version in " + outputFilePath);
        } catch (IOException e) {
            logger.error("Cannot write results on file", e);
            System.exit(-4);
        }
    }

    private static void checkArgsValidity(String[] args) {
        CommandOptions cmd = new CommandOptions(args);

        String swaggerFilePath = cmd.valueOf("--swagger");
        String outputFilePath = cmd.valueOf("--output");
        String logVerbosity = cmd.valueOf("--verbose");

        int help = cmd.searchOption("--help");
        int version = cmd.searchOption("--version");

        if (help >= 0 || version >= 0) {
            System.out.println(helpMessage);
            System.exit(0);
        }

        if (outputFilePath == null || outputFilePath.isEmpty()){
            System.err.println("Error -1: Invalid output file: " + outputFilePath);
            System.exit(-1);
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
        }
    }
}
