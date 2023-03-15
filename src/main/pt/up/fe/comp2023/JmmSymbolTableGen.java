package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

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
        String module = jmmNode.get("moduleName");
        this.imports.add(module);
        return null;
    }

    public String tableToString(String indentation){
        String showImports = (this.imports.size() > 0)? "Imports:\n":"";
        String showClasses = (this.classes.size() == 1)? "Class:\n":"Classes:\n";
        String thisIndentation = indentation + "  ";
        StringBuilder classes = new StringBuilder();
        for (ClassSymbolTable c : this.classes){
            classes.append(c.tableToString(thisIndentation));
        }
        StringBuilder imports = new StringBuilder();
        for (String c : this.imports){
            imports.append(thisIndentation).append(c).append("\n");
        }
        return showImports + imports + showClasses + classes;
    }
}
