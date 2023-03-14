package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class ClassSymbolTableGen extends AJmmVisitor<Void,Void> {
    ClassSymbolTable classTable;
    public ClassSymbolTableGen(){
        classTable = new ClassSymbolTable();
    }
    @Override
    protected void buildVisitor() {
            addVisit("ClassDeclaration",this::handleClassDeclaration);
            addVisit("MethodDeclaration",this::handleMethodDeclaration);
            addVisit("ClassVarDeclaration",this::handleVarClassDeclaration);

    }

    private Void handleVarClassDeclaration(JmmNode jmmNode, Void unused) {
        return null;
    }

    private Void handleClassDeclaration(JmmNode jmmNode, Void unused) {
        System.out.println("Handling Class");
        if(jmmNode.hasAttribute("extendsName")){
            classTable.setParentClass(jmmNode.get("extendsName"));
        }
        if(jmmNode.hasAttribute("isStatic")){
            classTable.setIsStatic(true);
        }


        return null;
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, Void unused) {
        MethodSymbolTableGen methodTableGen = new MethodSymbolTableGen(this.classTable);
        methodTableGen.visit(jmmNode);
        MethodSymbolTable methodSymbolTable = methodTableGen.getMethodTable();
        this.classTable.addMethod(methodSymbolTable);
        return null;
    }

    public ClassSymbolTable getClassTable(){
        return this.classTable;
    }


}
