package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

// DOes it mamke sense dooing this here and not while building the symbol table?
public class VarDeclarationAnalyser extends  Analyser<Void>{
    public VarDeclarationAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.setDefaultVisit((a,b)->null);
    }
}
