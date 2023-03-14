package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.LinkedList;
import java.util.List;

public class ScopeSymbolTableGen extends AJmmVisitor<Void, Void> {
    ScopeSymbolTable thisScope;
    public  ScopeSymbolTableGen(ScopeSymbolTable parentScope){
        this.thisScope = new ScopeSymbolTable();
        this.thisScope.setParentScope(parentScope);
    }
    @Override
    protected void buildVisitor() {
            addVisit("VarDeclarationStatement",this::handleVarDeclaration);
            addVisit("ScopedBlock",this::handleScopeBlock);
            this.setDefaultVisit(this::visitAllChildren);
    }


    private Void handleVarDeclaration(JmmNode jmmNode,Void unused) {
        String varName = jmmNode.get("varName");
        // Get identifier type type
        // I whish instead of isArray i could have dimensions
        Type t = new Type("int",false);
        Symbol s = new Symbol(t,varName);
        this.thisScope.addSymbol(s);
        return  null;

    }

    private Void handleScopeBlock(JmmNode jmmNode,Void unused) {
        ScopeSymbolTableGen childGen = new ScopeSymbolTableGen(this.thisScope);
        childGen.visit(jmmNode,unused);
        ScopeSymbolTable childScope = childGen.getScope();
        this.thisScope.addSubScope(childScope);
        return null;

    }

    private ScopeSymbolTable getScope() {
        return this.thisScope;
    }


}
