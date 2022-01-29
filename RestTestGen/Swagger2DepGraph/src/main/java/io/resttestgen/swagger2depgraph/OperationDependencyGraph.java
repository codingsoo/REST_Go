package io.resttestgen.swagger2depgraph;

import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Similar to the InputDependencyGraph
 * but instead of creating one edge for each common parameter, it creates just one edge, with a label
 * including the number of matching fields.
 */
public class OperationDependencyGraph extends InputDependencyGraph {

    private DirectedMultigraph<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> graph;
    private List<io.resttestgen.swagger2depgraph.OperationNode> nodes;
    private List<io.resttestgen.swagger2depgraph.OperationNode> leaves;


    public DirectedMultigraph<io.resttestgen.swagger2depgraph.OperationNode, RelationshipEdge> getGraph() {
        return graph;
    }

    public List<io.resttestgen.swagger2depgraph.OperationNode> getNodes() {
        return nodes;
    }

    public List<io.resttestgen.swagger2depgraph.OperationNode> getLeaves() {
        return leaves;
    }

    public Set<RelationshipEdge> getEdges() {
        return this.graph.edgeSet();
    }

    /**
     * Create a new InputDependencyGraph from Swagger File
     * @param swaggerPath path to swagger specification file (yaml or json)
     */
    public OperationDependencyGraph(String swaggerPath) {
        super(swaggerPath);
    }

    /**
     * Create a new InputDependencyGraph from OpenAPI POJO Object
     * @param openAPI OpenAPI POJO class
     * */
    public OperationDependencyGraph(OpenAPI openAPI) {
        super(openAPI);
    }

    @Override
    protected void initGraphFromSwagger(OpenAPI openAPI) {
        this.graph = new DirectedMultigraph<>(RelationshipEdge.class);
        this.nodes = new ArrayList<>();

        io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();

        // Add nodes to graph
        logger.info("Extracting nodes input and output from swagger schemas");
        List<SwaggerOperation> operations = SchemaExtractor.getOperationsList(openAPI);
        SchemaExtractor schemaExtractor = new SchemaExtractor(openAPI);
        for (SwaggerOperation operation : operations) {
            Operation operationSchema = operation.getOperationSchema();
            Set<String> inputParameters = schemaExtractor.extractInputParameters(operationSchema);
            Set<String> outputParameters = schemaExtractor.extractOutputParameters(operationSchema);
            io.resttestgen.swagger2depgraph.OperationNode operationNode = new io.resttestgen.swagger2depgraph.OperationNode(operationSchema, operation.getOperationId(), inputParameters, outputParameters);
            this.nodes.add(operationNode);
        }

        logger.info("Added " + nodes.size() + " nodes");
        for (io.resttestgen.swagger2depgraph.OperationNode node : nodes) {
            this.graph.addVertex(node);
        }

        // Add edges (labelled) with the common property
        /*
         * Description:
         *   Consider all the possible tuple of nodes.
         *   Nodes: 1, 2, 3, 4
         *   (1, 2)
         *   (1, 3)
         *   (1, 4)
         *   (2, 1)
         *   (2, 3)
         *   ...
         *
         *   For each tuple, add an edge if
         *   n1, n2 = tuple
         *   n1 input field is output of n2
         * */
        int numberOfNodes = nodes.size();
        for (int i = 0; i < numberOfNodes; i++) {
            for (int j = 0; j < numberOfNodes; j++) {
                if (i == j) continue;
                io.resttestgen.swagger2depgraph.OperationNode s = nodes.get(i);
                io.resttestgen.swagger2depgraph.OperationNode d = nodes.get(j);

                List<String> commonFields = getLinksWith(s, d);
                if (commonFields.size() > 0) {
                    String label = String.format("%s (%d)", d.getOperationId(), commonFields.size());
                    this.graph.addEdge(s, d, new RelationshipEdge(label));
                }
            }
        }

        // Get list of leaves
        // Nodes without outgoing edges
        this.leaves = this.graph.vertexSet().stream()
                .filter(v -> graph.outgoingEdgesOf(v).size() == 0)
                .collect(Collectors.toList());
    }
}
