package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.generators.symboltable.MethodSymbolTableGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import java.util.LinkedList;
import java.util.List;

public class MethodContextAnalyser extends ContextAnalyser<Void> {
    private MethodSymbolTable methodTable;

    public MethodContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
        // TODO: All of this will be done in the MethodTable Generator
        // TODO: method overloading proper implementation
        // TODO: method overloading error logging
        // This implies that the signuture is present
        context.setMethodContext(root.get("signature"));
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("VarTypeSpecification", this::handleVarDeclaration);
        this.addVisit("Statement", this::handleStatement);
        this.setDefaultVisit(this::visitAllChildren);
    }


    private Void handleStatement(JmmNode jmmNode, List<Report> reports) {
        StatementContextAnalyser st = new StatementContextAnalyser(jmmNode, symbolTable, context);
        reports.addAll(st.analyse());
        return null;
    }

}
