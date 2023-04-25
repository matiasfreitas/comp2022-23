package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.symboltable.MethodSymbolTableGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import java.util.LinkedList;
import java.util.List;

public class MethodContextAnalyser extends ContextAnalyser<Void> {
    private MethodSymbolTable methodTable;

    public MethodContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
        // All of this will be done in the MethodTable Generator
        MethodSymbolTableGen m = new MethodSymbolTableGen();
        List<Report> throwAway = new LinkedList<>();
        m.visit(root,throwAway);
        this.methodTable= m.getMethodTable();
        String methodRepresentaion =this.methodTable.getStringRepresentation();
        //System.out.println("Method Analyser of method with following representation: "+methodRepresentaion);
        context.setMethodContext(methodRepresentaion);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("VarDeclaration",this::handleVarDeclaration);
        this.addVisit("Statement",this::handleStatement);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleVarDeclaration(JmmNode jmmNode, List<Report> reports) {
        return null;
    }

    private Void handleStatement(JmmNode jmmNode, List<Report> reports) {
        StatementContextAnalyser st = new StatementContextAnalyser(jmmNode,symbolTable,context);
        reports.addAll(st.analyse());
        return null;
    }

}
