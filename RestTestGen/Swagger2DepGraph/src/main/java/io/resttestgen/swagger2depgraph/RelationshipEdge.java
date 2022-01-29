package io.resttestgen.swagger2depgraph;

import org.jgrapht.graph.DefaultEdge;

import java.util.Objects;

public class RelationshipEdge
        extends
        DefaultEdge
{
    private String label;

    /**
     * Constructs a relationship edge
     *
     * @param label the label of the new edge.
     *
     */
    public RelationshipEdge(String label)
    {
        this.label = label;
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
    }

    public Object getSourceNode() {
        return this.getSource();
    }

    public Object getTargetNode() {
        return this.getTarget();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationshipEdge)) return false;
        RelationshipEdge that = (RelationshipEdge) o;
        return Objects.equals(label, that.label) &&
                Objects.equals(getSourceNode(), that.getSourceNode()) &&
                Objects.equals(getTargetNode(), that.getTargetNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, getSourceNode(), getTargetNode());
    }
}
