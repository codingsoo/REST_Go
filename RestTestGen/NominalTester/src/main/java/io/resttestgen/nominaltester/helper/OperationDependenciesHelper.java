package io.resttestgen.nominaltester.helper;

import io.resttestgen.nominaltester.models.OperationInfo;
import io.resttestgen.swagger2depgraph.InputDependencyGraph;
import io.resttestgen.swagger2depgraph.OperationNode;
import io.resttestgen.swagger2depgraph.RelationshipEdge;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OperationDependenciesHelper contains all the dependencies of the method getOperationDependencies
 */
public class OperationDependenciesHelper {

    static final Logger logger = LogManager.getLogger(OperationDependenciesHelper.class);

    private Map<String, List<OperationInfo>> invocationClassOperationsMap;
    private InputDependencyGraph inputDependencyGraph;

    /**
     * OperationDependenciesHelper's constructor
     * @param pathOperationsMap Map< ApiClassName, List of Operation Info>
     * @param idg Instance of the input dependency graph
     */
    public OperationDependenciesHelper(Map<String, List<OperationInfo>> pathOperationsMap, InputDependencyGraph idg) {
        this.invocationClassOperationsMap = pathOperationsMap;
        this.inputDependencyGraph = idg;
    }

    /**
     * Get the list of suspected operations that needs to be executed before the target operation target
     * to its correct executions. If operationInfo is a DELETE, it probably needs an operation POST (add).
     *
     * We first an order between operations under the same package based on HTTP Method
     * GET, POST, PUT, DELETE
     *
     * Then, we can also use operation in a dependency graph
     *
     * @param target operation's that needs to be executed
     * @return Ordered list of operation to be executed before operationInfo
     */
    public Queue<OperationInfo> getOperationDependencies(OperationInfo target) {

        Queue<OperationInfo> operationDependencies = new LinkedList<>();

        if (!hasInputParameters(target.getOperationSchema())) {
            return operationDependencies;
        }

        // Sort operation by HTTP Method enum
        String targetInvocationClassName = target.getInvocationClassName();
        List<OperationInfo> operationInTheSameClass = this.invocationClassOperationsMap.get(targetInvocationClassName);
        operationInTheSameClass.sort(Comparator.comparing(OperationInfo::getHttpMethod));
        int targetOperationIndex = operationInTheSameClass.indexOf(target);
        List<OperationInfo> precedingOperations = operationInTheSameClass.subList(0, targetOperationIndex);

        // Filter all those preceding operation which do not require input parameters
        // List<OperationInfo> precedingOperationsWithNoInputs = precedingOperations.stream().
        //        filter(x -> !hasInputParameters(x.getOperationSchema())).collect(Collectors.toList());

        // Get leaves with no input parameters from input dependency schema
        OperationNode graphTargetNode = inputDependencyGraph.getNodeById(target.getOperationId());
        Graph<OperationNode, RelationshipEdge> subgraphWithLeaves =
                inputDependencyGraph.createFirstLevelSubgraph(graphTargetNode, true);

        Set<OperationNode> linkedLeaves = subgraphWithLeaves.vertexSet();
            linkedLeaves.stream().filter(x -> !hasInputParameters(x.getOperationSchema())).collect(Collectors.toList());

        // Get a OperationInfo's matching OperationId
        List<OperationInfo> operationFromGraph = new ArrayList<>();
        for (OperationInfo operationInfo : operationInTheSameClass) {
            for (OperationNode linkedLeaf : linkedLeaves) {
                if (operationInfo.getOperationId().equals(linkedLeaf.getOperationId())) {
                    operationFromGraph.add(operationInfo);
                }
            }
        }

        // add to operation dependencies
        operationDependencies.addAll(precedingOperations);
        for (OperationInfo operationInfo : operationFromGraph) {
            if (!operationInfo.equals(target) &&
                !operationDependencies.contains(operationInfo)) {
                operationDependencies.add(operationInfo);
            }
        }

        return operationDependencies;
    }


    /**
     * Check if the target operation needs some parameters
     * @param operationSchema target operation
     * @return boolean, true if has some input, false otherwise
     */
    private boolean hasInputParameters(Operation operationSchema) {
        List<Parameter> pathParameters = operationSchema.getParameters();
        RequestBody bodyParameter = operationSchema.getRequestBody();
        return pathParameters != null || bodyParameter != null;
    }



}
