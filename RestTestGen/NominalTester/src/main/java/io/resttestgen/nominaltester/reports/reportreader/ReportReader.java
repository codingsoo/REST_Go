package io.resttestgen.nominaltester.reports.reportreader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.reports.ReportCustomGson;
import io.swagger.v3.oas.models.OpenAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReportReader {
    private final Gson customGson;

    public ReportReader(OpenAPI openAPI) {
        this.customGson = ReportCustomGson.getCustomGson(openAPI);
    }

    /**
     * Reads a JSON report from a file > Transform it to ResponseCoverage
     * @param pathToJsonFile the path to the JSON report to read
     * @return returns the ResponseCoverage object for a given operation
     * @throws FileNotFoundException if the file cannot be read or does not exist
     */
    public ResponseCoverage readReport(String pathToJsonFile) throws FileNotFoundException {
        // Try to open the Json file
        JsonReader jsonReader = new JsonReader(new FileReader(pathToJsonFile));

        // Initialise the parser
        JsonParser parser = new JsonParser();

        // Get the root element
        JsonElement rootElement = parser.parse(jsonReader);

        // Parse the file and construct the operationCoverage class containing all information
        return customGson.fromJson(rootElement, ResponseCoverage.class);
    }

    /**
     * Reads one or more JSON reports from the specified directory
     * Assume you have just an OperationCoverage per OperationId
     * @param directory the directory containing the JSON reports
     * @return a Map of OperationCoverages mapped by operationId
     */
    public OperationCoverage readReportsFromDirectory(String directory) throws FileNotFoundException {
        OperationCoverage operationCoverage = new OperationCoverage();

        final File folder = new File(directory);
        List<String> reportFiles = listFilesForFolder(folder);
        for (String reportFile : reportFiles) {
            ResponseCoverage responseCoverage = readReport(reportFile);
            String operationId = responseCoverage.getTarget().getOperationId();
            OperationResult operationResult = new OperationResult(responseCoverage.getTarget());
            operationResult.setResponseCoverage(responseCoverage);
            operationResult.setOperationId(operationId);
            operationCoverage.addOperationResult(operationId, operationResult);
        }
        return operationCoverage;
    }

    /**
     * Returns the list of reports found in the specified directory
     * @param folder the folder where to look up for the report files
     * @return a list of strings containing the report files
     */
    private List<String> listFilesForFolder(File folder) {
        List<String> reports = new ArrayList<>();

        for (File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (!fileEntry.getName().equals("summary.json") && fileEntry.getName().endsWith(".json")) {
                reports.add(fileEntry.getAbsolutePath());
            }
        }

        return reports;
    }

}

