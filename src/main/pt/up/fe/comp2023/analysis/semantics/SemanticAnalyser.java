package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyser extends  Analyser<Void>{

    public SemanticAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ClassDeclaration",this::handleClassDeclaration);
        this.addVisit("ImportDeclaration",this::handleImports);
        this.addVisit("MethodDeclaration",this::handleMethodDeclaration);
        this.setDefaultVisit((a,b) -> null);

    }

    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Method Declaration");
        MethodAnalyser ma = new MethodAnalyser(jmmNode,symbolTable,context);
        List<Report>  methodReports = ma.analyse();
        reports.addAll(methodReports);
        context.setClassContext();
        return null;
    }

    private Void handleImports(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Import Declaration");
        return null;
    }

    private Void handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Class Declaration");
        return null;
    }

}
