package pt.up.fe.comp2023.analysis.symboltable.generators;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.symboltable.ClassSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MyJmmSymbolTable;

import java.util.LinkedList;
import java.util.List;

public class JmmSymbolTableGen extends AJmmVisitor<Void,Void> {
    List<String> imports;
    List<ClassSymbolTable> classes;
    public JmmSymbolTableGen(){
        imports = new LinkedList<>();
        classes= new LinkedList<>();
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
        classes.add(classGen.getClassTable());
        return null;
    }

    private Void handleImportDeclaration(JmmNode jmmNode, Void unused) {
        StringBuilder nameImport = new StringBuilder();
        for (Object value: (List) jmmNode.getObject("moduleName")) {
            nameImport.append(value);
            nameImport.append(".");

        }
        String module = nameImport.deleteCharAt(nameImport.length()).toString();

        this.imports.add(module);
        return null;
    }


    public JmmSymbolTable getJmmSymbolTable() {
        return new JmmSymbolTable(imports,classes.get(0));
    }

    public MyJmmSymbolTable getMyJmmSymbolTable() {
        return  new MyJmmSymbolTable(imports,classes);
    }
}
