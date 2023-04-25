package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

public class SemanticContextAnalyser extends ContextAnalyser<Void> {

    public SemanticContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ImportDeclaration",this::handleImports);
        this.addVisit("ClassDeclaration",this::handleClassDeclaration);
        this.addVisit("VarTypeSpecification", this::handleVarDeclaration);
        this.addVisit("MethodDeclaration",this::handleMethodDeclaration);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        System.out.println(jmmNode.getAttributes());
        Optional<String> hasSuper = jmmNode.getOptional("extendsName");
        if(hasSuper.isPresent()){
            Type superType = new Type(hasSuper.get(),false);
            if(!this.validType(superType)){
                reports.add(this.createReport(jmmNode,"This class extends undefined type "+ superType+ " maybe try importing it!"));
            }
        }
        return this.visitAllChildren(jmmNode,reports);
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
