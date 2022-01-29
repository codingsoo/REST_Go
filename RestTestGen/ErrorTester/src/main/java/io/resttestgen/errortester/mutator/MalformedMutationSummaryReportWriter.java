package io.resttestgen.errortester.mutator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Write operation coverage as report
 */
public class MalformedMutationSummaryReportWriter {

    /**
     * Returns custom Gson with adapters to handle serialization/deserialization of OperationCoverage object
     * @return gson object
     */
    private Gson getCustomGson() {
        GsonBuilder builder = new GsonBuilder();

        builder.setPrettyPrinting();

        return builder.create();
    }

    /**
     * Converts summary object to json string
     * @param summary object to convert
     * @return String representing JSON representation of operationCoverage object
     */
    private String toJson(Summary summary) {
        Gson gson = getCustomGson();
        return gson.toJson(summary);
    }

    /**
     * Writes Summary JSON representation on file
     * @param summary object to be written on file
     * @param filename filename of the output file
     * @throws IOException error during file writing
     */
    public void toJsonFile(Summary summary, String filename) throws IOException {
        String jsonRepresentation = toJson(summary);
        Path write = Files.write(Paths.get(filename), jsonRepresentation.getBytes());
    }
}
