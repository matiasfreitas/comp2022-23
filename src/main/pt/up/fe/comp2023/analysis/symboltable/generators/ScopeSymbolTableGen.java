package pt.up.fe.comp2023.analysis.symboltable.generators;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.symboltable.ScopeSymbolTable;

public class ScopeSymbolTableGen extends AJmmVisitor<Void, Void> {
    ScopeSymbolTable thisScope;

    public ScopeSymbolTableGen(ScopeSymbolTable parentScope) {
        this.thisScope = new ScopeSymbolTable();
        this.thisScope.setParentScope(parentScope);
    }

    @Override
    protected void buildVisitor() {
        addVisit("VarTypeSpecification", this::handleVarDeclaration);
        addVisit("ScopedBlock", this::handleScopeBlock);
        this.setDefaultVisit(this::visitAllChildren);
    }


    private Void handleVarDeclaration(JmmNode jmmNode, Void unused) {
        // System.out.println("Handling Var Declaration inside a scope");
        SymbolGen symbolGen = new SymbolGen();
        symbolGen.visit(jmmNode);
        Symbol s = symbolGen.getSymbol();
        //System.out.println(s.toString());
        this.thisScope.addSymbol(s);
        return null;

    }

    private Void handleScopeBlock(JmmNode jmmNode, Void unused) {
        //System.out.println("Handling new Scope inside scope");
        ScopeSymbolTableGen childGen = new ScopeSymbolTableGen(this.thisScope);
        for(JmmNode child : jmmNode.getChildren()){
            childGen.visit(child, unused);
        }
        ScopeSymbolTable childScope = childGen.getScope();
        this.thisScope.addSubScope(childScope);
        return null;

    }

    public ScopeSymbolTable getScope() {
        return this.thisScope;
    }

}