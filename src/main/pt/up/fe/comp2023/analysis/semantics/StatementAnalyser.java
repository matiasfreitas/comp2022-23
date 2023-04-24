package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

public class StatementAnalyser extends Analyser<Void> {

    public StatementAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("ReturnStatement", this::handleReturnStatement);
        this.addVisit("ArrayAssignment", this::handleArrayAssignment);
        this.addVisit("Assignment", this::handleAssignment);
        this.addVisit("SingleStatement", this::handleSingleStatement);
        this.addVisit("WhileLoop", this::handleWhileLoop);
        this.addVisit("IfStatement", this::handleIfStatement);

    }

    private Void handleIfStatement(JmmNode jmmNode, List<Report> reports) {
        return null;
    }

    private Void handleWhileLoop(JmmNode jmmNode, List<Report> reports) {
        return null;
    }

    private Void handleSingleStatement(JmmNode jmmNode, List<Report> reports) {
        ExpressionAnalyser ex = new ExpressionAnalyser(jmmNode, symbolTable, context);
        reports.addAll(ex.analyse());
        return null;
    }

    private Void handleAssignment(JmmNode jmmNode, List<Report> reports) {
        return null;
    }

    private Void handleArrayAssignment(JmmNode jmmNode, List<Report> reports) {
        String varName = jmmNode.get("varName");
        Optional<Type> maybeArrayType = this.checkIdentifier(varName, jmmNode, reports);
        if (maybeArrayType.isPresent()) {
            Type arrayType = maybeArrayType.get();
            if (!arrayType.isArray()) {
                reports.add(this.createReport(jmmNode, "Trying to index type that is not an array"));
            }
            JmmNode indexNode = jmmNode.getJmmChild(0);
            ExpressionAnalyser ex = new ExpressionAnalyser(indexNode, symbolTable, context);
            reports.addAll(ex.analyse());
            Optional<Type> maybeIndexType = ex.getType();
            if (maybeIndexType.isPresent() && !maybeIndexType.get().getName().equals("int")) {
                reports.add(this.createReport(jmmNode, "Array Index Must Be of Type integer"));
            }
            JmmNode expressionNode = jmmNode.getJmmChild(1);
            ex = new ExpressionAnalyser(expressionNode, symbolTable, context);
            reports.addAll(ex.analyse());
            if (maybeIndexType.isPresent() && !maybeIndexType.get().getName().equals(arrayType.getName())) {
                StringBuilder b = new StringBuilder("Trying to assign ");
                b.append(maybeIndexType.get().toString());
                b.append("To an array of ");
                b.append(arrayType.toString());
                reports.add(this.createReport(jmmNode, b.toString()));
            }
        }
        return null;
    }

    private Void handleReturnStatement(JmmNode jmmNode, List<Report> reports) {
        JmmNode expressionNode = jmmNode.getJmmChild(0);
        ExpressionAnalyser ex = new ExpressionAnalyser(expressionNode, symbolTable, context);
        reports.addAll(ex.analyse());
        Optional<Type> exType = ex.getType();
        Type methodReturnType = symbolTable.getReturnType(this.context.getMethodSignature());
        if (exType.isPresent() && !methodReturnType.equals(exType.get())) {
            StringBuilder error = new StringBuilder("Method Returns ");
            error.append(methodReturnType.toString())
                    .append(" But ")
                    .append(exType.get().toString())
                    .append(" is being returned");
            reports.add(this.createReport(jmmNode, error.toString()));
        }
        return null;
    }
}
