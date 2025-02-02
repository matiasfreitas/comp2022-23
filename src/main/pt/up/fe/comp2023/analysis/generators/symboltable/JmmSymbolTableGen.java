package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.semantics.Analyser;
import pt.up.fe.comp2023.analysis.symboltable.ClassSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.LinkedList;
import java.util.List;

public class JmmSymbolTableGen extends Analyser<Void> {
    List<String> imports;
    List<ClassSymbolTable> classes;

    public JmmSymbolTableGen(JmmNode root) {
        super(root);
        imports = new LinkedList<>();
        classes = new LinkedList<>();
    }

    @Override
    protected void buildVisitor() {
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        this.setDefaultVisit(this::visitAllChildren);

    }

    private Void handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        ClassSymbolTableGen classGen = new ClassSymbolTableGen(jmmNode);
        reports.addAll(classGen.analyse());
        classes.add(classGen.getClassTable());
        return null;
    }

    private Void handleImportDeclaration(JmmNode jmmNode, List<Report> reports) {
        this.imports.add(getImportString(jmmNode));
        return null;
    }

    public static String getImportString(JmmNode jmmNode) {
        String module = jmmNode.get("moduleName");
        return stringToList(module);
    }

    private static String stringToList(String moduleString) {
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < moduleString.length(); i++) {
            var c = moduleString.charAt(i);
            if (c == '[' || c == ' ') {
                continue;
            } else if (c == ']') {
                break;
            } else if (c == ',') {
                current.append('.');
            } else {
                current.append(c);
            }
        }
        return current.toString();
    }


    public JmmSymbolTable getJmmSymbolTable() {
        return new JmmSymbolTable(imports, classes.get(0));
    }

}
