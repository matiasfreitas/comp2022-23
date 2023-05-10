package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

public class StatementContextAnalyser extends ContextAnalyser<Void> {

    public StatementContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
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
        this.setDefaultVisit(this::visitAllChildren);

    }

    private Void handleCondition(JmmNode jmmNode, List<Report> reports) {
        ExpressionContextAnalyser ex = new ExpressionContextAnalyser(jmmNode, symbolTable, context);
        reports.addAll(ex.analyse());
        Optional<Type> conditionType = ex.getType();
        if (conditionType.isPresent() && !JmmBuiltins.typeEqualOrAssumed(conditionType.get(), JmmBuiltins.JmmBoolean)) {
            String message = "Condition expression evaluates to " + conditionType.get() + " but should evaluate to boolean";
            reports.add(this.createErrorReport(jmmNode, message));
        }
        return null;
    }

    private Void handleIfStatement(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Visiting if statement");
        JmmNode conditionNode = jmmNode.getJmmChild(0);
        this.handleCondition(conditionNode, reports);
        // Handle if
        this.visit(jmmNode.getJmmChild(1), reports);
        // Handle  else
        return this.visit(jmmNode.getJmmChild(2), reports);
    }

    private Void handleWhileLoop(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Visiting while loop");
        JmmNode conditionNode = jmmNode.getJmmChild(0);
        this.handleCondition(conditionNode, reports);
        // Handle while scope
        return this.visit(jmmNode.getJmmChild(1), reports);
    }

    private boolean isValidSingleStatement(JmmNode jmmNode) {
        var kind = jmmNode.getKind();
        return kind.equals("MethodCalling") || kind.equals("NewObject");

    }

    private Void handleSingleStatement(JmmNode jmmNode, List<Report> reports) {
        var expression = jmmNode.getJmmChild(0);
        ExpressionContextAnalyser ex = new ExpressionContextAnalyser(expression, symbolTable, context);
        reports.addAll(ex.analyse());
        if (!isValidSingleStatement(expression)) {
            reports.add(createErrorReport(jmmNode, "Not a valid statement!"));
        }
        if (JmmBuiltins.fromAnnotatedNode(expression).equals(JmmBuiltins.JmmAssumeType)) {
            reports.add(createTypeAssumptionWarning(jmmNode, JmmBuiltins.JmmVoid));
            JmmBuiltins.annotate(expression, JmmBuiltins.JmmVoid);
        }

        return null;
    }

    private Void handleAssignment(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Visiting assignment statement");
        String varName = jmmNode.get("varName");
        Optional<Type> maybeType = this.checkIdentifier(varName, jmmNode, reports);
        // TODO: what about inheritance
        if (maybeType.isPresent()) {
            Type type = maybeType.get();
            JmmNode expressionNode = jmmNode.getJmmChild(0);
            ExpressionContextAnalyser ex = new ExpressionContextAnalyser(expressionNode, symbolTable, context);
            reports.addAll(ex.analyse());
            Optional<Type> maybeAssignedType = ex.getType();
            if (maybeAssignedType.isPresent()) {
                Type assignedType = maybeAssignedType.get();
                // TODO: If the type is imported we assume to be using it correctly
                if (this.symbolTable.isImportedSymbol(assignedType.getName())) {
                    return null;
                }
                if (!JmmBuiltins.typeEqualOrAssumed(type, assignedType)) {
                    boolean thisClass = this.symbolTable.isThisClassType(assignedType.getName());
                    String thisClassSuper = this.symbolTable.getSuper();
                    if (thisClass && thisClassSuper != null && thisClassSuper.equals(type.getName())) {
                        return null;
                    }
                    StringBuilder b = new StringBuilder("Trying to assign ");
                    b.append(assignedType);
                    b.append("To a variable of type ");
                    b.append(type);
                    reports.add(this.createErrorReport(jmmNode, b.toString()));
                }
            }
        }
        return null;

    }

    private Void handleArrayAssignment(JmmNode jmmNode, List<Report> reports) {
        // System.out.println("Visiting array assignment statement");
        String varName = jmmNode.get("varName");
        Optional<Type> maybeArrayType = this.checkIdentifier(varName, jmmNode, reports);
        if (maybeArrayType.isPresent()) {
            Type arrayType = maybeArrayType.get();
            if (!arrayType.isArray()) {
                reports.add(this.createErrorReport(jmmNode, "Trying to index type that is not an array"));
            }
            JmmNode indexNode = jmmNode.getJmmChild(0);
            ExpressionContextAnalyser ex = new ExpressionContextAnalyser(indexNode, symbolTable, context);
            reports.addAll(ex.analyse());
            Optional<Type> maybeIndexType = ex.getType();
            if (maybeIndexType.isPresent() && !maybeIndexType.get().getName().equals("int")) {
                reports.add(this.createErrorReport(jmmNode, "Array Index Must Be of Type integer"));
            }
            JmmNode expressionNode = jmmNode.getJmmChild(1);
            ex = new ExpressionContextAnalyser(expressionNode, symbolTable, context);
            reports.addAll(ex.analyse());
            Optional<Type> maybeAssignType = ex.getType();
            Type acceptsType = new Type(arrayType.getName(), false);
            if (maybeAssignType.isPresent() && !JmmBuiltins.typeEqualOrAssumed(acceptsType, maybeAssignType.get())) {
                StringBuilder b = new StringBuilder("Trying to assign ");
                b.append(maybeAssignType.get());
                b.append("To an array of ");
                b.append(acceptsType);
                reports.add(this.createErrorReport(jmmNode, b.toString()));
            }
        }
        return null;
    }

    private Void handleReturnStatement(JmmNode jmmNode, List<Report> reports) {
        // System.out.println("Visiting Return statement");
        JmmNode expressionNode = jmmNode.getJmmChild(0);
        ExpressionContextAnalyser ex = new ExpressionContextAnalyser(expressionNode, symbolTable, context);
        reports.addAll(ex.analyse());
        Optional<Type> exType = ex.getType();
        String thisMethod = this.context.getMethodSignature();
        //System.out.println("We are returning from " + thisMethod);
        Type methodReturnType = symbolTable.getReturnType(thisMethod);
        if (exType.isPresent() && !JmmBuiltins.typeEqualOrAssumed(methodReturnType, exType.get())) {
            StringBuilder error = new StringBuilder("Method Returns ");
            error.append(methodReturnType)
                    .append(" But ")
                    .append(exType.get())
                    .append(" is being returned");
            reports.add(this.createErrorReport(jmmNode, error.toString()));
        }
        return null;
    }
}
