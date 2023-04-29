package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;
import java.util.List;

public class OllirGenerator extends AJmmVisitor<List<Report>, String> {
    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("Assignment",this::handleAssignment);
    }



    private String defaultVisit(JmmNode node, List<Report> reports) {
        System.out.println("Visiting node " + node.getKind());
        var code = new StringBuilder();
        for (var child : node.getChildren()) {
            code.append(visit(child, reports));
        }
        return code.toString();
    }

    private String handleImportDeclaration(JmmNode jmmNode, List<Report> reports) {

        return defaultVisit(jmmNode,reports);
    }

    private String handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        return defaultVisit(jmmNode,reports);
    }

    private String handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        return defaultVisit(jmmNode,reports);
    }
    private String handleAssignment(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Assignemtn");

        return "";
    }
}
