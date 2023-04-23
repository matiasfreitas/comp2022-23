package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class Analyser<T> extends PostorderJmmVisitor<List<Report>,T> {
    protected JmmSymbolTable symbolTable;
    protected JmmNode root;

    protected UsageContext context;

    public Analyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        this.root = root;
        this.symbolTable = symbolTable;
        this.context = context;
    }

    public List<Report> analyse() {
        List<Report> reports = new LinkedList<>();
        this.visit(this.root, reports);
        return reports;
    }

    protected Report createReport(JmmNode node, String message) {
        int line = Integer.parseInt(node.get("LINE"));
        int column = Integer.parseInt(node.get("COLUMN"));
        return Report.newError(Stage.SEMANTIC, line, column, message, null);

    }

}
