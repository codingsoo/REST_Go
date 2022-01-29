package io.resttestgen.errortester.mutator;

import com.google.gson.Gson;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.reports.ReportCustomGson;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Write operation coverage as report
 */
public class MalformedMutationReportWriter {

    Gson customGson;

    public MalformedMutationReportWriter(OpenAPI openAPI) {
        customGson = ReportCustomGson.getCustomGson(openAPI);
    }
    /**
     * Converts operation coverage object to json string
     * @param responseCoverage object to convert
     * @return String representing JSON representation of operationCoverage object
     */
    private String toJson(ResponseCoverage responseCoverage) {
        return customGson.toJson(responseCoverage);
    }

    /**
     * Writes operationCoverage JSON representation on file
     * @param responseCoverage object to be written on file
     * @param filename filename of the output file
     * @throws IOException error during file writing
     */
    public void toJsonFile(ResponseCoverage responseCoverage, String filename) throws IOException {
        String jsonRepresentation = toJson(responseCoverage);
        Path write = Files.write(Paths.get(filename), jsonRepresentation.getBytes());
    }
}
