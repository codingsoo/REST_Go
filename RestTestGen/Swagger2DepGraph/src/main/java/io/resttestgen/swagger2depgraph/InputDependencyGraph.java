package io.resttestgen.swagger2depgraph;

import io.resttestgen.swaggerschema.SchemaExtractor;
import io.resttestgen.swaggerschema.models.SwaggerOperation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InputDependencyGraph {

    static final Logger logger = LogManager.getLogger(InputDependencyGraph.class);

    private DirectedMultigraph<OperationNode, RelationshipEdge> graph;
    private List<OperationNode> nodes;
    private List<OperationNode> leaves;
    private OpenAPI openAPI;

    public DirectedMultigraph<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> getGraph() {
        return graph;
    }

    public List<io.resttestgen.swagger2depgraph.OperationNode> getNodes() {
        return nodes;
    }

    public List<io.resttestgen.swagger2depgraph.OperationNode> getLeaves() {
        return leaves;
    }

    public Set<io.resttestgen.swagger2depgraph.RelationshipEdge> getEdges() {
        return this.graph.edgeSet();
    }

    /**
     * Create a new InputDependencyGraph from Swagger File
     * @param swaggerPath path to swagger specification file (yaml or json)
     */
    public InputDependencyGraph(String swaggerPath) {
        this.openAPI = new OpenAPIV3Parser().read(swaggerPath);
        this.initGraphFromSwagger(this.openAPI);
    }

    /**
     * Create a new InputDependencyGraph from OpenAPI POJO Object
     * @param openAPI OpenAPI POJO class
     * */
    public InputDependencyGraph(OpenAPI openAPI) {
        this.initGraphFromSwagger(openAPI);
    }

    /**
     * Create a directed graph from OpenAPI pojo object
     *
     * Graph is described in this way:
     * - OperationNode (Nodes): an object identifying the endpoint
     * - Relationship (Links): there is an edge between two nodes A and B if a field that is in A's input set is B's output
     *
     * RelationshipEdge has a label identifying that input parameter
     * @param openAPI POJO object from swagger
     */
    protected void initGraphFromSwagger(OpenAPI openAPI) {
        logger.info("Creating the Input Dependency Graph");
        this.graph = new DirectedMultigraph<>(io.resttestgen.swagger2depgraph.RelationshipEdge.class);
        this.nodes = new ArrayList<>();

        io.swagger.v3.oas.models.Paths paths = openAPI.getPaths();
        Set<Map.Entry<String, PathItem>> entries = paths.entrySet();

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
                for (String commonField : commonFields) {
                    String tmp = commonField;
                    this.graph.addEdge(s, d, new io.resttestgen.swagger2depgraph.RelationshipEdge(tmp));
                }
            }
        }
        logger.info("Added " + this.graph.edgeSet().size() + " edges");


        // Get list of leaves
        // Nodes without outgoing edges
        this.leaves = this.graph.vertexSet().stream()
                .filter(v -> graph.outgoingEdgesOf(v).size() == 0)
                .collect(Collectors.toList());
    }

    public io.resttestgen.swagger2depgraph.OperationNode getNodeById(String operationId) {
        return this.nodes.stream().filter(n -> n.getOperationId().equalsIgnoreCase(operationId)).findFirst().orElse(null);
    }

    /**
     * Returns the list of inputs fields of source which are part of the output of the dest.
     * @param source node with inputs parameters
     * @param dest node with output parameters
     * @return List of source's input fields common with node's output fields
     */
    protected List<String> getLinksWith(io.resttestgen.swagger2depgraph.OperationNode source, io.resttestgen.swagger2depgraph.OperationNode dest) {
        Set<io.resttestgen.swagger2depgraph.OperationParameter> intersection = new HashSet<>(source.getInputParameters());
        intersection.retainAll(dest.getOutputParameters());
        return intersection.stream().map(io.resttestgen.swagger2depgraph.OperationParameter::getParameterName).collect(Collectors.toList());
    }

    /**
     *
     * Returns a subgraph with the targetRoot node and its neighbors leaves
     *
     * @param targetRoot target node we want to test
     * @param leavesOnly true to filter out only target's neighbors that are not leaves
     * @return sub-graph containing target node with its outgoing edges to its neighbors
     */
    public Graph<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> createFirstLevelSubgraph(io.resttestgen.swagger2depgraph.OperationNode targetRoot,
                                                                                                                           boolean leavesOnly) {
        Set<io.resttestgen.swagger2depgraph.OperationNode> subsetVertex = new HashSet<>();

        // Get all the neighbors
        Set<io.resttestgen.swagger2depgraph.OperationNode> rootNeighbors = getGraph().outgoingEdgesOf(targetRoot).stream()
                .map(e -> (io.resttestgen.swagger2depgraph.OperationNode) e.getTargetNode())
                .collect(Collectors.toSet());

        // Filter all neighbors that are also leaves
        if (leavesOnly) {
            rootNeighbors = rootNeighbors.stream().filter(getLeaves()::contains).collect(Collectors.toSet());
        }

        // add vertexes
        subsetVertex.add(targetRoot);
        subsetVertex.addAll(rootNeighbors);

        // add edges
        Set<io.resttestgen.swagger2depgraph.RelationshipEdge> subsetEdges = new HashSet<>(getGraph().outgoingEdgesOf(targetRoot));

        // create subgraph
        return new AsSubgraph<>(getGraph(), subsetVertex, subsetEdges);
    }

    /**
     * Returns a subgraph with the targetRoot node and its neighbors leaves
     *
     * @param targetRoot target node we want to test
     * @param leavesOnly true to filter out only target's neighbors that are not leaves
     * @param pattern regex to filter out outgoing edges
     * @return sub-graph containing target node with its outgoing edges to its neighbors
     */
    public Graph<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> createFirstLevelSubgraph(io.resttestgen.swagger2depgraph.OperationNode targetRoot,
                                                                                                                           boolean leavesOnly,
                                                                                                                           Pattern pattern) {
        Set<io.resttestgen.swagger2depgraph.OperationNode> subsetVertex = new HashSet<>();

        // Get all the neighbors
        Set<io.resttestgen.swagger2depgraph.OperationNode> rootNeighbors = graph.outgoingEdgesOf(targetRoot).stream()
                .map(e -> (io.resttestgen.swagger2depgraph.OperationNode) e.getTargetNode())
                .collect(Collectors.toSet());

        // Filter all neighbors that are also leaves
        if (leavesOnly) {
            rootNeighbors = rootNeighbors.stream().filter(leaves::contains).collect(Collectors.toSet());
        }

        // add vertexes
        subsetVertex.add(targetRoot);
        subsetVertex.addAll(rootNeighbors);

        // add edges with filters
        Set<io.resttestgen.swagger2depgraph.RelationshipEdge> outgoingEdges = graph.outgoingEdgesOf(targetRoot);
        Set<io.resttestgen.swagger2depgraph.RelationshipEdge> subsetEdges = outgoingEdges.stream()
                .filter(e-> pattern.matcher(e.getLabel()).find())
                .collect(Collectors.toSet());

        // create subgraph
        return new AsSubgraph<>(graph, subsetVertex, subsetEdges);
    }

    /**
     * Writes a InputDependencyGraph as a Dot file
     * @param outputFilename Path of the output file, es. output.dot
     * @return boolean, true if success, false if not
     */
    public boolean writeDotGraph(String outputFilename) {
        try {
            // providers define how vertexes, labels and edges should be rendered,
            ComponentNameProvider<io.resttestgen.swagger2depgraph.OperationNode> vertexIdProvider = io.resttestgen.swagger2depgraph.OperationNode::getOperationId;
            ComponentNameProvider<io.resttestgen.swagger2depgraph.OperationNode> vertexLabelProvider = io.resttestgen.swagger2depgraph.OperationNode::getOperationId;
            ComponentNameProvider<io.resttestgen.swagger2depgraph.RelationshipEdge> edgeLabelProvider = io.resttestgen.swagger2depgraph.RelationshipEdge::getLabel;

            GraphExporter<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> exporter =
                    new DOTExporter<>(vertexIdProvider, vertexLabelProvider, edgeLabelProvider);

            Writer writer = new FileWriter(outputFilename);
            exporter.exportGraph(this.getGraph(), writer);
            logger.info("Write graph in " + outputFilename);
        } catch (IOException | ExportException e) {
            logger.error("Unable to write graph on file: " + outputFilename + " - " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Writes a InputDependencyGraph as a Dot file
     *
     * @param g Graph to write on file
     * @param outputFilename Path of the output file, es. output.dot
     * @throws IOException
     * @throws ExportException
     */
    public static void writeGraphOnDotFile(Graph<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> g, String outputFilename)
            throws IOException, ExportException {

        // providers define how vertexes, labels and edges should be rendered,
        ComponentNameProvider<io.resttestgen.swagger2depgraph.OperationNode> vertexIdProvider = io.resttestgen.swagger2depgraph.OperationNode::getOperationId;
        ComponentNameProvider<io.resttestgen.swagger2depgraph.OperationNode> vertexLabelProvider = io.resttestgen.swagger2depgraph.OperationNode::getOperationId;
        ComponentNameProvider<io.resttestgen.swagger2depgraph.RelationshipEdge> edgeLabelProvider = io.resttestgen.swagger2depgraph.RelationshipEdge::getLabel;

        GraphExporter<io.resttestgen.swagger2depgraph.OperationNode, io.resttestgen.swagger2depgraph.RelationshipEdge> exporter =
                new DOTExporter<>(vertexIdProvider, vertexLabelProvider, edgeLabelProvider);

        Writer writer = new FileWriter(outputFilename);
        exporter.exportGraph(g, writer);
    }
    
}
