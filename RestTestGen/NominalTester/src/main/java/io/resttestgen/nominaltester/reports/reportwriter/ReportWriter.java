package io.resttestgen.nominaltester.reports.reportwriter;

import com.google.gson.Gson;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.models.summaries.OperationCoverageSummary;
import io.resttestgen.nominaltester.reports.ReportCustomGson;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Write operation coverage as report
 */
public class ReportWriter {

    private Path outputDir;
    private Gson gson;

    public ReportWriter(OpenAPI openAPI, String outputDir) {
        this.outputDir = Paths.get(outputDir);
        boolean mkdirs = this.outputDir.toFile().mkdirs();
        this.gson = ReportCustomGson.getCustomGson(openAPI);
    }

    public ReportWriter(OpenAPI openAPI, Path outputDir) {
        this.outputDir = outputDir;
        boolean mkdirs = this.outputDir.toFile().mkdirs();
        this.gson = ReportCustomGson.getCustomGson(openAPI);
    }

    /**
     * Converts operation coverage object to json string
     * @param responseCoverage object to convert
     * @return String representing JSON representation of operationCoverage object
     */
    public String toJson(ResponseCoverage responseCoverage) {
        return this.gson.toJson(responseCoverage);
    }

    /**
     * Writes operationCoverage JSON representation on file
     * @param responseCoverage object to be written on file
     * @param filename filename of the output file
     * @throws IOException error during file writing
     */
    public void toJsonFile(ResponseCoverage responseCoverage, String filename) throws IOException {
        String jsonRepresentation = toJson(responseCoverage);
        Path write = Files.write(outputDir.resolve(filename + ".json"), jsonRepresentation.getBytes());
    }

    public void writeOperationCoverage(OperationCoverage operationCoverage) throws IOException {
        OperationCoverageSummary report = operationCoverage.getReport();
        String operationCoverageReportJson = this.gson.toJson(report);
        Path write = Files.write(outputDir.resolve("summary" + ".json"), operationCoverageReportJson.getBytes());
    }

}
