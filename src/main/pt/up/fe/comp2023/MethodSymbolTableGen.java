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
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, Void unused) {
        System.out.println(jmmNode.toTree());
        return null;
    }


    public MethodSymbolTable getMethodTable() {
        return  this.thisMethod;
    }
}
