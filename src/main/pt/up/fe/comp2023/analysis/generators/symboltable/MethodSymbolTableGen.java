package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.ClassSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.ScopeSymbolTable;

import java.util.List;

public class MethodSymbolTableGen extends AJmmVisitor<List<Report>,Void> {
    MethodSymbolTable thisMethod;
    public  MethodSymbolTableGen(){
        this.thisMethod = new MethodSymbolTable();
    }
    @Override
    protected void buildVisitor() {
        addVisit("MethodDeclaration",this::handleMethodDeclaration);
        addVisit("MethodBody",this::handleMethodBody);
        addVisit("MethodArguments",this::handleMethodArguments);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodArguments(JmmNode jmmNode, List<Report> reports) {
        // TODO: Symbolos com o mesmo nome?
        for(JmmNode child : jmmNode.getChildren()) {
            SymbolGen sGen = new SymbolGen();
            sGen.visit(child);
            this.thisMethod.addParameter(sGen.getSymbol());
        }
        return null;
    }

    private Void handleMethodBody(JmmNode jmmNode, List<Report>reports) {
        //System.out.println("Handling Method Body");
        ScopeSymbolTableGen scopeTableGen = new ScopeSymbolTableGen(null);
        scopeTableGen.visit(jmmNode,reports);
        ScopeSymbolTable methodScope = scopeTableGen.getScope();
        thisMethod.setMethodScope(methodScope);
        return null;
    }
    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        String visibility = "private";
        boolean isStatic = false;
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
        for (JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals("Type")) {
                TypeGen typeGen = new TypeGen();
                typeGen.visit(child);
                this.thisMethod.setReturnType(typeGen.getType());
            }
            else{
                visit(child,reports);
            }
        }
        return null;
    }


    public MethodSymbolTable getMethodTable() {
        return  this.thisMethod;
    }
}
