package io.resttestgen.nominaltester.models.coverage;

import io.resttestgen.nominaltester.models.TestCase;
import io.resttestgen.swagger2depgraph.RelationshipEdge;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TransitionCoverage extends Coverage {

    Map<RelationshipEdge, TestCase> edgeCoverageMap;

    public TransitionCoverage(Set<RelationshipEdge> edges) {
        edgeCoverageMap = new HashMap<>();
        for (RelationshipEdge edge : edges) {
            edgeCoverageMap.put(edge, null);
        }
    }

    public Map<RelationshipEdge, TestCase> getEdgeCoverageMap() {
        return edgeCoverageMap;
    }

    public void addTransitionCoverage(RelationshipEdge edge, TestCase testCase) {
        edgeCoverageMap.put(edge, testCase);
    }

    public TestCase getTransitionCoverage(RelationshipEdge edge) {
        return edgeCoverageMap.get(edge);
    }

    @Override
    public String toString() {
        int totalNumberOfTransitions = edgeCoverageMap.size();
        int numberOfNotNullTransitions = (int) edgeCoverageMap.values().stream().filter(Objects::nonNull).count();
        double successfulPercentage = (double)numberOfNotNullTransitions / (double)totalNumberOfTransitions * 100;
        return String.format("%d transitions; %d with a 2xx test case (%s%%)", totalNumberOfTransitions, numberOfNotNullTransitions, new DecimalFormat("#.##").format(successfulPercentage));
    }
}
