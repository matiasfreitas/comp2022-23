package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class MethodSymbolTableGen extends AJmmVisitor<Void,Void> {
    MethodSymbolTable thisMethod;
    public  MethodSymbolTableGen(ClassSymbolTable parentClass){
        this.thisMethod = new MethodSymbolTable();
        this.thisMethod.setParentClass(parentClass);
    }
    @Override
    protected void buildVisitor() {
        addVisit("MethodDeclaration",this::handleMethodDeclaration);
        addVisit("MethodBody",this::handleMethodBody);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodBody(JmmNode jmmNode, Void unused) {
        System.out.println("Handling Method Body");
        ScopeSymbolTableGen scopeTableGen = new ScopeSymbolTableGen(null);
        scopeTableGen.visit(jmmNode);
        ScopeSymbolTable methodScope = scopeTableGen.getScope();
        thisMethod.setMethodScope(methodScope);
        return null;
        
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, Void unused) {
        System.out.println("Handling Method");

        for(JmmNode child : jmmNode.getChildren()) {
            visit(child);
        }
        return null;
    }


    public MethodSymbolTable getMethodTable() {
        return  this.thisMethod;
    }
}
