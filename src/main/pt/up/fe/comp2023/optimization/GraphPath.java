package pt.up.fe.comp2023.optimization;

import java.util.Objects;

public class GraphPath {
    GraphVertice first;
    GraphVertice second;

    public GraphPath(GraphVertice vertice1, GraphVertice vertice2) {
        first = vertice1;
        second = vertice2;
    }

    public GraphVertice getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GraphPath graphPath = (GraphPath) object;
        return Objects.equals(first, graphPath.first) && Objects.equals(second, graphPath.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

}