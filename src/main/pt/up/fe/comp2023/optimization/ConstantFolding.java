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

    private Void handleBinaryOp(JmmNode jmmNode, Void unused) {
        System.out.println("Seeing binary operator");
        return null;
    }

    private Void handleUnary(JmmNode jmmNode, Void unused) {
        System.out.println("Seeing unary operator");
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
