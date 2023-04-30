package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.semantics.UsageContext;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.LinkedList;
import java.util.List;

public class OllirGenerator extends AJmmVisitor<List<Report>,String> {
    private OllirExpressionGenerator exprGen;
    private JmmSymbolTable symbolTable;

    public OllirGenerator(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.exprGen = new OllirExpressionGenerator(symbolTable);

    }



    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("Assignment", this::handleAssignment);
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

        return defaultVisit(jmmNode, reports);
    }

    private String handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        symbolTable.setCurrentMethod(jmmNode.get("signature"));
        String methodCode = defaultVisit(jmmNode, reports);
        symbolTable.setCurrentMethod(null);
        return methodCode;
    }

    private String handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        return defaultVisit(jmmNode, reports);
    }

    private String handleAssignment(JmmNode jmmNode, List<Report> reports) {
        System.out.println(jmmNode.toTree());
        // This assumes typechecking was already done
        var rhs = exprGen.visit(jmmNode.getJmmChild(0));
        var code = new StringBuilder(rhs.code());
        code.append(jmmNode.get("varName"))
                .append(".")
                .append(rhs.symbol().type())
                .append(" :=")
                .append(".")
                .append(rhs.symbol().type())
                .append(" ")
                .append(rhs.symbol().toCode())
                .append(";\n");

        return code.toString();
    }
}
