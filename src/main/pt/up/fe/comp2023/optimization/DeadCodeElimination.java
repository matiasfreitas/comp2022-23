package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.JmmBuiltins;

import java.util.Optional;

public class DeadCodeElimination extends JmmIterativeOptimizer {
    private enum Condition {
        True, False, NotKnown;
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        startOptimizing();
        visit(semanticsResult.getRootNode());
        return semanticsResult;
    }

    @Override
    protected void buildVisitor() {
        this.setDefaultVisit(this::visitAllChildren);
        addVisit("IfStatement", this::handleIf);
        addVisit("WhileLoop", this::handleWhile);

    }

    private Condition evaluateCondition(JmmNode jmmNode) {
        if (!JmmBuiltins.isLiteralNode(jmmNode)) {
            return Condition.NotKnown;
        }
        if (jmmNode.get("value").equals("true")) {
            return Condition.True;
        }
        return Condition.False;

    }

    private Void handleWhile(JmmNode jmmNode, Void unused) {
        if (!evaluateCondition(jmmNode.getJmmChild(0)).equals(Condition.False)) {
            return null;
        }
        didOptimize();
        jmmNode.getJmmParent().removeJmmChild(jmmNode);
        return null;
    }

    private Void handleIf(JmmNode jmmNode, Void unused) {
        Condition result = evaluateCondition(jmmNode.getJmmChild(0));
        if (result.equals(Condition.NotKnown)) {
            return null;
        }
        didOptimize();
        JmmNode chosenBlock = jmmNode.getJmmChild(1);
        if (result.equals(Condition.False)) {
            chosenBlock = jmmNode.getJmmChild(2);
        }
        jmmNode.replace(chosenBlock);
        return null;
    }
}
