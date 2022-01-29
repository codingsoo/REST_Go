package io.resttestgen.nominaltester.testers.operationtestingstrategies;

import io.resttestgen.nominaltester.cli.ExAppConfig;
import io.resttestgen.nominaltester.fieldgenerator.RandomGenerator;
import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.nominaltester.models.OperationResult;
import io.resttestgen.nominaltester.models.ResponseDictionary;
import io.resttestgen.nominaltester.models.coverage.Coverage;
import io.resttestgen.nominaltester.models.coverage.OperationCoverage;
import io.resttestgen.nominaltester.models.coverage.ResponseCoverage;
import io.resttestgen.nominaltester.reports.reportwriter.ReportWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.JunitWriter;
import io.resttestgen.nominaltester.testcases.junitwriter.exceptions.JunitBuilderException;
import io.resttestgen.nominaltester.testers.DictionaryBasedTester;
import io.resttestgen.nominaltester.testers.OperationTester;
import io.resttestgen.swagger2depgraph.InputDependencyGraph;
import io.resttestgen.swagger2depgraph.OperationNode;
import io.resttestgen.swagger2depgraph.RelationshipEdge;
import io.resttestgen.swaggerschema.models.exceptions.SchemaValidationException;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyGraphBasedOperationTester extends OperationTester implements DictionaryBasedTester {

    static final Integer TIME_BUDGET_SECONDS = 0; // Default time budget of 10 minutes

    static final Logger logger = LogManager.getLogger(DependencyGraphBasedOperationTester.class);

    private InputDependencyGraph inputDependencyGraph;
    private FuzzingWithDictionaryOperationTester fuzzingWithDictionaryOperationTester;
    private ReportWriter reportWriter;
    private JunitWriter junitWriter;

    public DependencyGraphBasedOperationTester(OpenAPI openAPI, Map<String, List<OperationInfo>> operationsPerApiClass) throws SchemaValidationException {
        super(openAPI, operationsPerApiClass);
        inputDependencyGraph = new InputDependencyGraph(openAPI);
        fuzzingWithDictionaryOperationTester = new FuzzingWithDictionaryOperationTester(openAPI, operationsPerApiClass);
        initializeWriters(ExAppConfig.outputFolder);
        inputDependencyGraph.writeDotGraph(ExAppConfig.outputFolder + "/input-dependency-graph.dot");
    }

    /**
     * Initialize the object junit-writer and report-writer
     * @param outputFolder output folder for the reports
     */
    private void initializeWriters(String outputFolder) {
        // Instantiate writers
        reportWriter = new ReportWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"reports/"));
        junitWriter = new JunitWriter(this.openAPI, Paths.get(ExAppConfig.outputFolder,"src/test/java/"));
    }

    @Override
    public void setAuthHookClass(Class<?> authHookClass) {
        super.setAuthHookClass(authHookClass);
        fuzzingWithDictionaryOperationTester.setAuthHookClass(authHookClass);
    }

    @Override
    public void setResetHookClass(Class<?> resetHookClass) {
        super.setResetHookClass(resetHookClass);
        fuzzingWithDictionaryOperationTester.setResetHookClass(resetHookClass);
    }

    /**
     * Implements the DependencyGraph based strategy
     * It starts testing the leaves, and goes up, visiting all the other nodes up to the root
     * If there are cycles, consider as leaves a random node in the graph
     * @return OperationCoverage object with processed operation and their results
     */
    @Override
    public Coverage run() {
        OperationCoverage operationCoverage = new OperationCoverage();

        List<OperationInfo> operations = this.operationsPerApiClass.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());

        // Time budget per operation is total time budget divided by number of operations divided by 2 executions
        // (one execution is Nominal Tester, the other one is the Error Tester)
        Integer operationTimeBudget = 1;
        // Integer operationTimeBudget = TIME_BUDGET_SECONDS / operations.size() / 2;

        operationCoverage.setNumberOfDocumentedOperations(operations.size());

        RandomGenerator randomGenerator = new RandomGenerator();
        Set<OperationNode> testedOperations = new HashSet<>();
        Set<OperationNode> newlyTestedOperations = new HashSet<>();

        DirectedMultigraph<OperationNode, RelationshipEdge> graph = inputDependencyGraph.getGraph();
        List<OperationResult> successfulSteps = new ArrayList<>();

        while (graph.vertexSet().size() > 0) {

            // Get leaves
            List<OperationNode> leaves = getLeaves(graph);

            // Order them for number of input parameters
            // Not all the leaves are without parameters
            // It is leaf if no input parameter can be linked
            leaves.sort(Comparator.comparing(a -> a.getInputParameters().size()));

            // If there are only cycles, select a random vertex as leaf
            if (leaves.size() == 0) {
                leaves.add(randomGenerator.getRandomElementFromCollection(graph.vertexSet()));
            }

            // Test leaves first
            for (OperationNode leaf : leaves) {
                OperationInfo matchingOperationInfo = getMatchingOperationInfo(leaf);
                OperationResult operationResult = fuzzingWithDictionaryOperationTester.testOperation(matchingOperationInfo, operationTimeBudget);
                if (operationResult.getResponseCoverage().containsSuccessfulExecution()) {
                    newlyTestedOperations.add(leaf);
                }
                operationCoverage.addOrMergeOperationResult(matchingOperationInfo.getOperationId(), operationResult);
                writeOperationResultOnFile(matchingOperationInfo, operationResult);
                writeOperationCoverage(operationCoverage);
            }

            // while there are always newly tested operations
            while (newlyTestedOperations.size() > 0) {
                Set<OperationNode> recentlyTestedOperations = new HashSet<>();

                // test newly tested linked nodes that we didn't test yet
                for (OperationNode newlyTestedOperation : newlyTestedOperations) {
                    Set<OperationNode> linkedNodes = getLinkedNodes(graph, newlyTestedOperation);

                    linkedNodes.removeAll(testedOperations);
                    linkedNodes.removeAll(recentlyTestedOperations);
                    linkedNodes.removeAll(newlyTestedOperations);

                    for (OperationNode linkedNode : linkedNodes) {
                        OperationInfo matchingOperationInfo = getMatchingOperationInfo(linkedNode);
                        OperationResult operationResult = fuzzingWithDictionaryOperationTester.testOperation(matchingOperationInfo, operationTimeBudget);
                        if (operationResult.getResponseCoverage().containsSuccessfulExecution()) {
                            recentlyTestedOperations.add(linkedNode);
                        }
                        String operationId = matchingOperationInfo.getOperationId();
                        operationCoverage.addOrMergeOperationResult(operationId, operationResult);
                        writeOperationResultOnFile(matchingOperationInfo, operationCoverage.getOperationCoverage(operationId));
                        writeOperationCoverage(operationCoverage);
                    }
                }

                // move newly tested operations -> tested operations
                testedOperations.addAll(newlyTestedOperations);

                // assign recently_tested operations to newly_tested_operations_list
                newlyTestedOperations = recentlyTestedOperations;
            }

            // remove all the tested operations from the graph
            graph.removeAllVertices(testedOperations);

            // remove also leaves, because their are untestable
            graph.removeAllVertices(leaves);
        }

        logger.info("All operations have been processed");
        return operationCoverage;
    }

    /**
     * Get leaves of the graph
     * @param graph directed graph
     * @return Set of OperationNode
     */
    private List<OperationNode> getLeaves(Graph<OperationNode, RelationshipEdge> graph){
        return graph.vertexSet().stream()
                .filter(v -> graph.outgoingEdgesOf(v).size() == 0)
                .collect(Collectors.toList());
    }

    /**
     * Get the matching OperationInfo object to the Graph's OperationNode vertex
     * @param operationNode operation vertex
     * @return matching OperationInfo object
     */
    private OperationInfo getMatchingOperationInfo(OperationNode operationNode) {
        String targetOperationId = operationNode.getOperationId();
        List<OperationInfo> operations = this.operationsPerApiClass.values().stream()
                .flatMap(Collection::stream).collect(Collectors.toList());
        OperationInfo matchingOperationInfo = operations.stream().filter(o -> o.getOperationId().equals(targetOperationId)).findFirst().orElse(null);
        if (matchingOperationInfo == null) throw new NoSuchElementException();
        return matchingOperationInfo;
    }

    /**
     * Gets all the incoming edges of the target node
     * incoming edges link the target node, with the operations
     * which depend of it for some parameter
     *
     * @param graph directed graph with dependencies, getPetById depends on getPets (getPetById -id-> getPets)
     * @param target target node
     * @return Set of OperationNode which depends on target node result
     */
    private Set<OperationNode> getLinkedNodes(Graph<OperationNode, RelationshipEdge> graph, OperationNode target) {
        Set<RelationshipEdge> relationshipEdges = graph.incomingEdgesOf(target);
        return relationshipEdges.stream().map(e -> (OperationNode)e.getSourceNode()).collect(Collectors.toSet());
    }

    /**
     * Writes JUnit test cases and ResponseCoverage JSON on files
     * @param operation tested operation
     * @param operationResult operation results object
     */
    private void writeOperationResultOnFile(OperationInfo operation, OperationResult operationResult) {
        String operationId = operation.getOperationId();
        ResponseCoverage responseCoverage = operationResult.getResponseCoverage();
        logger.info(operationResult.getResponseCoverage().toString());
        try {
            // logger.info("Writing report on file /reports/" + operation.getOperationId() + ".json");
            // reportWriter.toJsonFile(responseCoverage, operation.getOperationId());
            logger.info("Writing junit test cases /src/test/java/" + operation.getOperationId() + "_*.java");
            junitWriter.fromResponseCoverage(responseCoverage);
        } catch (IOException | JunitBuilderException e) {
            logger.error("Cannot write logs", e);
        }
    }

    /**
     * Makes ReportWriter write OperationCoverage on file
     * @param operationCoverage
     */
    private void writeOperationCoverage(OperationCoverage operationCoverage) {
        try {
            reportWriter.writeOperationCoverage(operationCoverage);
        } catch (IOException e) {
            logger.error("Cannot write logs", e);
        }
    }

    @Override
    public ResponseDictionary getResponseDictionary() {
        return this.fuzzingWithDictionaryOperationTester.getResponseDictionary();
    }

    @Override
    public void setResponseDictionary(ResponseDictionary responseDictionary) {
        this.fuzzingWithDictionaryOperationTester.setResponseDictionary(responseDictionary);
    }
}
