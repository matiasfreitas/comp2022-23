package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;

public class StatementAnalyser extends  Analyser<Void>{

    public StatementAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ReturnStatement",this::handleReturnStatement);
        this.addVisit("ArrayAssignment",this::handleArrayAssignment);
        this.addVisit("Assignment",this::handleAssignment);
        this.addVisit("SingleStatement",this::handleSingleStatement);
        this.addVisit("WhileLoop",this::handleWhileLoop);
        this.addVisit("IfStatement",this::handleIfStatement);

    }

    private Void handleIfStatement(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }

    private Void handleWhileLoop(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }

    private Void handleSingleStatement(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }

    private Void handleAssignment(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }

    private Void handleArrayAssignment(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }

    private Void handleReturnStatement(JmmNode jmmNode, List<Report> reports) {
        return  null;
    }
}
