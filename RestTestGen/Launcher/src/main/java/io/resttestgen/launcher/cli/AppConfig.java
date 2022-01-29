package io.resttestgen.launcher.cli;

public class AppConfig {

    /* Input filenames */
    final public String configurationFilename = "resttestgen_config.json";
    public String workingDirectory;
    public String openApiSpecificationFilename = "openapi_specification.json";
    public String editedOpenApiSpecificationFilename = "rtg_openapi_specification.json";
    public String codegenClassesJarFilename = "swagger-java-client-1.0.0-all.jar";

    /* Execution  */
    public Boolean runCodegen = true;
    public Boolean runNominalTester = true;
    public Boolean runErrorTester = true;

    /* Output filenames */
    public String outputDirectory = "output/";
    public String codegenOutputDirectory = "codegen/";
    public String nominalTesterOutputDirectory = "nominal_tester/";
    public String errorTesterOutputDirectory = "error_tester/";


    public AppConfig() {
        super();
    }

    public AppConfig(AppConfig appConfig) {
        this.workingDirectory = appConfig.workingDirectory;
        this.openApiSpecificationFilename = appConfig.openApiSpecificationFilename;
        this.codegenClassesJarFilename = appConfig.codegenClassesJarFilename;
        this.runCodegen = appConfig.runCodegen;
        this.runNominalTester = appConfig.runNominalTester;
        this.runErrorTester = appConfig.runErrorTester;
        this.outputDirectory = appConfig.outputDirectory;
        this.codegenOutputDirectory = appConfig.codegenOutputDirectory;
        this.nominalTesterOutputDirectory = appConfig.nominalTesterOutputDirectory;
        this.errorTesterOutputDirectory = appConfig.errorTesterOutputDirectory;
    }

    public void importUserConfig(UserConfig userConfig) {
        if (userConfig.openApiSpecificationFilename != null) {
            this.openApiSpecificationFilename = userConfig.openApiSpecificationFilename;
        }
        if (userConfig.outputDirectory != null) {
            this.outputDirectory = userConfig.outputDirectory;
        }
        if (userConfig.codegenOutputDirectory != null) {
            this.codegenOutputDirectory = userConfig.codegenOutputDirectory;
        }
        if (userConfig.nominalTesterOutputDirectory != null) {
            this.nominalTesterOutputDirectory = userConfig.nominalTesterOutputDirectory;
        }
        if (userConfig.errorTesterOutputDirectory != null) {
            this.errorTesterOutputDirectory = userConfig.errorTesterOutputDirectory;
        }
    }
}
