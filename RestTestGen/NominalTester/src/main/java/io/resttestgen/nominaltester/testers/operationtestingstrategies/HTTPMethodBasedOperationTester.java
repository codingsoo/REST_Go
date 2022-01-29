package io.resttestgen.nominaltester.testers.operationtestingstrategies;

import io.resttestgen.nominaltester.cli.ExAppConfig;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.coverage.Coverage;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.reports.reportwriter.ReportWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.JunitWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.exceptions.JunitBuilderException;
import io.resttestgen.nominaltester.testers.DictionaryBasedTester;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tester class contains methods and fields required to test operations
 * E.g. It has a response dictionary which is used during the parameter generation and
 * the method testOperation to execute the operation, getting the coverage
 */
public class HTTPMethodBasedOperationTester extends FuzzingWithDictionaryOperationTester implements DictionaryBasedTester {

    static final Logger logger = LogManager.getLogger(HTTPMethodBasedOperationTester.class);

    public HTTPMethodBasedOperationTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException {
        super(openAPI, operationsPerApiClass);
    }

    @Override
    public Coverage run() {
        OperationCoverage operationCoverage = new OperationCoverage();

        // Sort operation by HTTPMethod
        List<OperationInfo> operations = this.operationsPerApiClass.values().stream()
                .flatMap(Collection::stream).sorted(Comparator.comparing(OperationInfo::getHttpMethod))
                .collect(Collectors.toList());

        operationCoverage.setNumberOfDocumentedOperations(operations.size());

        // Instantiate writers
        ReportWriter reportWriter = new ReportWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"reports/"));
        JunitWriter junitWriter = new JunitWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"src/test/java/"));

        for (OperationInfo operation : operations) {
            try {
                logger.info("Testing operation " + operation.toString());
                OperationResult operationResult = testOperation(operation);
                ResponseCoverage responseCoverage = operationResult.getResponseCoverage();
                operationCoverage.addOperationResult(operation.getOperationId(), operationResult);
                logger.info(operationResult.getResponseCoverage().toString());
                logger.info("Writing report on file /reports/" + operation.getOperationId() + ".json");
                reportWriter.toJsonFile(responseCoverage, operation.getOperationId());
                logger.info("Writing junit test cases /src/test/java/" + operation.getOperationId() + "_*.java");
                junitWriter.fromResponseCoverage(responseCoverage);
            } catch (IOException | JunitBuilderException e) {
                logger.error("Cannot write report/tests", e);
                operationCoverage.addOperationResult(operation.getOperationId(), new OperationResult(operation));
            }

            try {
                reportWriter.writeOperationCoverage(operationCoverage);
            } catch (IOException e) {
                logger.error("Cannot write summary report", e);
                e.printStackTrace();
            }
        }

        logger.info("All the operations have been processed");

        return operationCoverage;
    }
}
