package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.LinkedList;
import java.util.List;

public class JmmSymbolTableGen extends AJmmVisitor<Void,Void> {
    List<String> imports;
    ClassSymbolTable classTable;
    public JmmSymbolTableGen(){
        imports = new LinkedList<>();
        classTable = new ClassSymbolTable();
    }

    @Override
    protected void buildVisitor() {
        addVisit("ImportDeclaration",this::handleImportDeclaration);
        addVisit("ClassDeclaration",this::handleClassDeclaration);
        this.setDefaultVisit(this::visitAllChildren);

    }

    private Void handleClassDeclaration(JmmNode jmmNode, Void unused) {
        ClassSymbolTableGen classGen = new ClassSymbolTableGen();
        classGen.visit(jmmNode);
        this.classTable = classGen.getClassTable();
        return null;
    }

    private Void handleImportDeclaration(JmmNode jmmNode, Void unused) {
        String module = jmmNode.get("moduleName");
        this.imports.add(module);
        return null;
    }
}
