package pt.up.fe.comp2023.optimization;

import java.util.*;

public class GraphVertice {
    String varName;
    int originalReg;
    ArrayList<GraphPath> paths;
    boolean active;

    public GraphVertice(String name, int reg) {
        varName = name;
        originalReg = reg;
        paths = new ArrayList<>();
        active = true;
    }

    public void addPath(GraphPath path) {
        if (!paths.contains(path))
            paths.add(path);
    }

    public void removePath(GraphPath path) {
        paths.remove(path);
    }

    public int getNPaths() {
        return getLinkedVertices().size();
    }

    public int getReg() {
        return originalReg;
    }

    public String getName() { return varName; }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public ArrayList<GraphPath> removePaths(){
        ArrayList<GraphPath> copy = paths;
        for (GraphPath path: paths) {
            removePath(path);
        }
        return copy;
    }

    public ArrayList<String> getLinkedVertices() {
        ArrayList<String> vars = new ArrayList<>();
        for (GraphPath path: paths) {
            if (path.getSecond().isActive())
                vars.add(path.getSecond().getName());
        }
        return vars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphVertice graphVertice = (GraphVertice) o;
        return originalReg == graphVertice.originalReg && Objects.equals(varName, graphVertice.varName) && Objects.equals(paths, graphVertice.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varName, originalReg, paths);
    }

}