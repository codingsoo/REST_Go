package io.resttestgen.launcher.cli;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class UserConfig {

    @SerializedName("openapi_specification_filename")
    public String openApiSpecificationFilename;

    @SerializedName("output_directory")
    public String outputDirectory;

    @SerializedName("codegen_output_directory")
    public String codegenOutputDirectory;

    @SerializedName("nominal_tester_output_directory")
    public String nominalTesterOutputDirectory;

    @SerializedName("error_tester_output_directory")
    public String errorTesterOutputDirectory;

    public UserConfig(Path configPath) throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(configPath);
        UserConfig tempUserConfig = gson.fromJson(reader, UserConfig.class);
        this.openApiSpecificationFilename = tempUserConfig.openApiSpecificationFilename;
        this.outputDirectory = tempUserConfig.outputDirectory;
        this.codegenOutputDirectory = tempUserConfig.codegenOutputDirectory;
        this.nominalTesterOutputDirectory = tempUserConfig.nominalTesterOutputDirectory;
        this.errorTesterOutputDirectory = tempUserConfig.errorTesterOutputDirectory;
    }
}
