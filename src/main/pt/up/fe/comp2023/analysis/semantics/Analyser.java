package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.LinkedList;
import java.util.List;

public abstract class Analyser<T> extends AJmmVisitor<List<Report>, T> {

    protected JmmNode root;

    public Analyser(JmmNode root){
        this.root = root;
    }

    public List<Report> analyse() {
        List<Report> reports = new LinkedList<>();
        this.visit(this.root, reports);
        return reports;
    }

    protected Report createReport(JmmNode node, String message) {
        int line = Integer.parseInt(node.get("lineStart"));
        int column = Integer.parseInt(node.get("colStart"));
        return Report.newError(Stage.SEMANTIC, line, column, message, null);

    }
}
