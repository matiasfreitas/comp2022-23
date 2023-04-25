package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;

public class SemanticContextAnalyser extends ContextAnalyser<Void> {

    public SemanticContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ImportDeclaration",this::handleImports);
        this.addVisit("MethodDeclaration",this::handleMethodDeclaration);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Method Declaration");
        MethodContextAnalyser ma = new MethodContextAnalyser(jmmNode,symbolTable,context);
        List<Report>  methodReports = ma.analyse();
        reports.addAll(methodReports);
        context.setClassContext();
        return null;
    }

    private Void handleImports(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Import Declaration");
        return null;
    }
}
