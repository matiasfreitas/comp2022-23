package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Arrays;
import java.util.List;

public class ConstantFolding extends AJmmVisitor<Void, Void> {

    private final List<String> literals = Arrays.asList("Int", "Boolean", "Char", "String", "Identifier", "This");

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::noOptimization);
        addVisit("Unary", this::handleUnary);
        addVisit("BinaryOp", this::handleBinaryOp);
        addVisit("Paren", this::handleParen);

    }

    private boolean literalNode(JmmNode jmmNode) {
        return literals.contains(jmmNode.getKind());
    }

    private Void handleParen(JmmNode jmmNode, Void unused) {
        var child = jmmNode.getJmmChild(0);
        visit(child);
        var foldedChild = jmmNode.getJmmChild(0);
        if (!literalNode(foldedChild)) {
            return null;
        }
        jmmNode.replace(foldedChild);
        return null;

    }

    private Void noOptimization(JmmNode jmmNode, Void unused) {
        for (var child : jmmNode.getChildren()) {
            visit(child);
        }
        return null;
    }

    private boolean isBinaryNodeKind(JmmNode jmmNode, String kind) {
        return jmmNode.getJmmChild(0).getKind().equals(kind)
                && jmmNode.getJmmChild(1).getKind().equals(kind);
    }

    private boolean canFoldBinaryNode(JmmNode jmmNode) {
        boolean areInt = isBinaryNodeKind(jmmNode, "Int");
        boolean areBool = isBinaryNodeKind(jmmNode, "Boolean");
        return areInt || areBool;
    }

    private void foldBooleanOperation(JmmNode jmmNode) {
        boolean lhs = jmmNode.getJmmChild(0).get("value").equals("true");
        boolean rhs = jmmNode.getJmmChild(1).get("value").equals("true");
        boolean result = lhs;
        if (jmmNode.get("op").equals("&&")) {
            result = lhs && rhs;
        }
        var newNode = new JmmNodeImpl("Boolean");
        newNode.put("value", String.valueOf(result));
        jmmNode.replace(newNode);
    }

    private void foldIntegerOperation(JmmNode jmmNode) {
        int lhs = Integer.parseInt(jmmNode.getJmmChild(0).get("value"));
        int rhs = Integer.parseInt(jmmNode.getJmmChild(1).get("value"));
        var operation = jmmNode.get("op");
        if (operation.equals("<")) {
            boolean result = lhs < rhs;
            var newNode = new JmmNodeImpl("Boolean");
            newNode.put("value", String.valueOf(result));
            jmmNode.replace(newNode);
            return;
        }
        int result = switch (operation) {
            case "+" -> lhs + rhs;
            case "-" -> lhs - rhs;
            case "*" -> lhs * rhs;
            case "/" -> lhs / rhs;
            default -> 0;
        };
        var newNode = new JmmNodeImpl("Int");
        newNode.put("value", String.valueOf(result));
        jmmNode.replace(newNode);

    }

    private Void handleBinaryOp(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getJmmChild(0));
        visit(jmmNode.getJmmChild(1));
        if (!canFoldBinaryNode(jmmNode)) {
            return null;
        }
        if (isBinaryNodeKind(jmmNode, "Boolean")) {
            foldBooleanOperation(jmmNode);
        } else {
            foldIntegerOperation(jmmNode);
        }
        return null;
    }

    private Void handleUnary(JmmNode jmmNode, Void unused) {
        JmmNode child = jmmNode.getJmmChild(0);
        visit(child);
        JmmNode foldedChild = jmmNode.getJmmChild(0);
        if (!foldedChild.getKind().equals("Boolean")) {
            return null;
        }
        boolean value = foldedChild.get("value").equals("true");
        boolean result = !value;
        JmmNode n = new JmmNodeImpl("Boolean");
        n.put("value", String.valueOf(result));
        jmmNode.replace(n);
        return null;
    }

    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        visit(semanticsResult.getRootNode());
        return semanticsResult;
    }

}
