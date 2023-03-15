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
        addVisit("MethodArguments",this::handleMethodArguments);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodArguments(JmmNode jmmNode, Void unused) {
        SymbolGen sGen = new SymbolGen();
        sGen.visit(jmmNode);
        this.thisMethod.addParameter(sGen.getSymbol());
        return null;
    }

    private Void handleMethodBody(JmmNode jmmNode, Void unused) {
        //System.out.println("Handling Method Body");
        ScopeSymbolTableGen scopeTableGen = new ScopeSymbolTableGen(null);
        scopeTableGen.visit(jmmNode);
        ScopeSymbolTable methodScope = scopeTableGen.getScope();
        thisMethod.setMethodScope(methodScope);
        return null;
    }
    private Void handleMethodDeclaration(JmmNode jmmNode, Void unused) {
        String visibility = "private";
        Boolean isStatic = false;
        if(jmmNode.hasAttribute("visibility")){
            visibility = jmmNode.get("visibility");
        }
        if(jmmNode.hasAttribute("isStatic"))
            isStatic = true;

        String methodName = jmmNode.get("methodName");
        //System.out.println("Method " + methodName + " isStatic " + isStatic +" visibility " + visibility);
        this.thisMethod.setMethodName(methodName);
        this.thisMethod.setIsStatic(isStatic);
        this.thisMethod.setVisibility(visibility);
        this.visitAllChildren(jmmNode,unused);
        return null;
    }


    public MethodSymbolTable getMethodTable() {
        return  this.thisMethod;
    }
}
