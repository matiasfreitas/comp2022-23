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
        this.setDefaultVisit((a,b) -> null);

    }

    private Void handleCondition(JmmNode jmmNode,List<Report> reports){
        ExpressionAnalyser ex = new ExpressionAnalyser(jmmNode, symbolTable, context);
        reports.addAll(ex.analyse());
        Optional<Type> conditionType = ex.getType();
        if(conditionType.isPresent() && !conditionType.get().getName().equals("boolean")){
            String message = "Condition expression evaluates to "+ conditionType.get()+ " but should evaluate to boolean";
            reports.add(this.createReport(jmmNode,message));
        }
        return null;
    }

    private Void handleIfStatement(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting if statement");
       JmmNode conditionNode = jmmNode.getJmmChild(0);
       return this.handleCondition(conditionNode,reports);

    }

    private Void handleWhileLoop(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting while loop");
        JmmNode conditionNode = jmmNode.getJmmChild(0);
        return this.handleCondition(conditionNode,reports);
    }

    private Void handleSingleStatement(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting single statement");
        ExpressionAnalyser ex = new ExpressionAnalyser(jmmNode, symbolTable, context);
        reports.addAll(ex.analyse());
        return null;
    }

    private Void handleAssignment(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting assignment statement");
        String varName = jmmNode.get("varName");
        Optional<Type> maybeType = this.checkIdentifier(varName, jmmNode, reports);
        if (maybeType.isPresent()) {
            Type type = maybeType.get();
            JmmNode expressionNode = jmmNode.getJmmChild(1);
            ExpressionAnalyser ex = new ExpressionAnalyser(expressionNode, symbolTable, context);
            reports.addAll(ex.analyse());
            Optional<Type> maybeAssignedType = ex.getType();
            if (maybeAssignedType.isPresent() && !maybeAssignedType.get().equals(type)) {
                StringBuilder b = new StringBuilder("Trying to assign ");
                b.append(maybeAssignedType.get());
                b.append("To a variable of type ");
                b.append(type);
                reports.add(this.createReport(jmmNode, b.toString()));
            }
        }
        return null;

    }

    private Void handleArrayAssignment(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting array assignment statement");
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
            Optional<Type> maybeAssignType = ex.getType();
            if (maybeAssignType.isPresent() && !maybeAssignType.get().getName().equals(arrayType.getName())) {
                StringBuilder b = new StringBuilder("Trying to assign ");
                b.append(maybeAssignType.get());
                b.append("To an array of ");
                b.append(arrayType);
                reports.add(this.createReport(jmmNode, b.toString()));
            }
        }
        return null;
    }

    private Void handleReturnStatement(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Visiting Return statement");
        JmmNode expressionNode = jmmNode.getJmmChild(0);
        ExpressionAnalyser ex = new ExpressionAnalyser(expressionNode, symbolTable, context);
        reports.addAll(ex.analyse());
        Optional<Type> exType = ex.getType();
        Type methodReturnType = symbolTable.getReturnType(this.context.getMethodSignature());
        if (exType.isPresent() && !methodReturnType.equals(exType.get())) {
            StringBuilder error = new StringBuilder("Method Returns ");
            error.append(methodReturnType)
                    .append(" But ")
                    .append(exType.get())
                    .append(" is being returned");
            reports.add(this.createReport(jmmNode, error.toString()));
        }
        return null;
    }
}
