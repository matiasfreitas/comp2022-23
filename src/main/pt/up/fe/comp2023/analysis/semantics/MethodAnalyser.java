package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.symboltable.MethodSymbolTableGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import java.util.List;
import java.util.function.BiFunction;

public class MethodAnalyser extends  Analyser<Void>{
    private MethodSymbolTable methodTable;

    public MethodAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
        // All of this will be done in the MethodTable Generator
        MethodSymbolTableGen m = new MethodSymbolTableGen();
        m.visit(root);
        this.methodTable= m.getMethodTable();
        String methodRepresentaion =this.methodTable.getStringRepresentation();

        context.setMethodContext(methodRepresentaion);
    }

    @Override
    protected void buildVisitor() {
        this.setDefaultVisit((a,b)->null);
        this.addVisit("VarDeclaration",this::handleVarDeclaration);
        this.addVisit("Statement",this::handleStatement);

    }

    private Void handleVarDeclaration(JmmNode jmmNode, List<Report> reports) {
    }

    private Void handleStatement(JmmNode jmmNode, List<Report> reports) {
        return o;
    }

}
