package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.JmmBuiltins;

import java.util.Optional;

public class DeadCodeElimination extends JmmIterativeOptimizer {
    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        startOptimizing();
        visit(semanticsResult.getRootNode());
        return semanticsResult;
    }

    @Override
    protected void buildVisitor() {
        this.setDefaultVisit(this::visitAllChildren);
        //addVisit("IfStatement", this::handleIf);
        addVisit("WhileLoop", this::handleWhile);

    }

    private Void handleWhile(JmmNode jmmNode, Void unused) {
        //       | 'while' '(' expression ')' statement #WhileLoop
        var expression = jmmNode.getJmmChild(0);
        if (!JmmBuiltins.isLiteralNode(expression)) {
            return null;
        }
        if (expression.get("value").equals("true")) {
            return null;
        }
        didOptimize();
        jmmNode.getJmmParent().removeJmmChild(jmmNode);
        return null;
    }

    private Void handleIf(JmmNode jmmNode, Void unused) {
        //'if' '(' expression ')' statement 'else' statement  #IfStatement
        return null;
    }
}
