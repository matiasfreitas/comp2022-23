package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

// DOes it mamke sense dooing this here and not while building the symbol table?
public class VarDeclarationContextAnalyser extends ContextAnalyser<Void> {
    public VarDeclarationContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {

    }
}
