package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.generators.symboltable.JmmSymbolTableGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.*;

public class SemanticContextAnalyser extends ContextAnalyser<Void> {

    Map<String, JmmNode> importNodes;

    public SemanticContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
        importNodes = new HashMap<>();
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ImportDeclaration", this::handleImports);
        this.addVisit("ClassDeclaration", this::handleClassDeclaration);
        this.addVisit("VarTypeSpecification", this::handleVarDeclaration);
        this.addVisit("MethodDeclaration", this::handleMethodDeclaration);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println(jmmNode.getAttributes());
        Optional<String> hasSuper = jmmNode.getOptional("extendsName");
        if (hasSuper.isPresent()) {
            Type superType = new Type(hasSuper.get(), false);
            if (!this.validType(superType)) {
                reports.add(this.createErrorReport(jmmNode, "This class extends undefined type " + superType + " maybe try importing it!"));
            }
        }
        return this.visitAllChildren(jmmNode, reports);
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Method Declaration");
        MethodContextAnalyser ma = new MethodContextAnalyser(jmmNode, symbolTable, context);
        // TODO: refacto type gen
        TypeGen tg = new TypeGen();
        System.out.println(jmmNode.getJmmChild(0).toTree());
        tg.visit(jmmNode.getJmmChild(0));
        Type returnType = tg.getType();
        if(!validType(returnType)){
            reports.add(createErrorReport(jmmNode.getJmmChild(0),"Type " + returnType + " is not an available type"));
        }
        List<Report> methodReports = ma.analyse();
        reports.addAll(methodReports);
        context.setClassContext();
        return null;
    }

    private Void handleImports(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Import Declaration");
        // TODO: isto precisa de um refactor são duas classes diferentes
        String fullModule = JmmSymbolTableGen.getImportString(jmmNode);
        Type importType = JmmSymbolTable.getImportTypeFromString(fullModule);
        String typeName = importType.getName();
        JmmNode isImported = importNodes.get(importType.getName());
        if (isImported != null) {
            // TODO: Do something with the other jmmNode to lead to the first import
            reports.add(this.createWarningReport(jmmNode, "Module" + importType + " is already being imported"));
        } else {
            importNodes.put(typeName, jmmNode);
        }
        return null;
    }

    public Report createUnusedImportReport(String importName) {
        JmmNode importNode = importNodes.get(importName);
        return createWarningReport(importNode, "Unused import : " + importName);

    }

    @Override
    public List<Report> analyse() {
        List<Report> reports = new LinkedList<>();
        this.visit(this.root, reports);
        this.symbolTable.getImportsUsage().forEach((importV, usage) -> {
            System.out.println(importV);
            if (usage.equals(0)) {
                reports.add(createUnusedImportReport(importV));
            }
        });
        return reports;
    }
}
