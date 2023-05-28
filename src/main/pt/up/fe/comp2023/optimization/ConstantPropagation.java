package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.JmmBuiltins;

import java.util.HashMap;
import java.util.Map;

public class ConstantPropagation extends AJmmVisitor<Void, Void> {
    Map<String, JmmNode> varValue;

    public ConstantPropagation() {
        varValue = new HashMap<>();
    }

    private void storeNode(String varName, JmmNode jmmNode) {
        var copyNode = JmmBuiltins.newNode(jmmNode);
        varValue.put(varName, copyNode);
    }

    private JmmNode loadNode(String varName) {
        var node = varValue.get(varName);
        return JmmBuiltins.newNode(node);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::visitAllChildren);
        addVisit("Assignment", this::handleWrite);
        addVisit("Identifier", this::handleRead);
    }

    private Void handleRead(JmmNode jmmNode, Void unused) {
        var varName = jmmNode.get("value");
        if (!varValue.containsKey(varName)) {
            return null;
        }
        jmmNode.replace(loadNode(varName));
        return null;
    }

    private Void handleWrite(JmmNode jmmNode, Void unused) {
        visit(jmmNode.getJmmChild(0));
        var child = jmmNode.getJmmChild(0);
        if (!JmmBuiltins.isLiteralNode(child)) {
            return null;
        }
        var varName = jmmNode.get("varName");
        storeNode(varName, child);
        return null;
    }

    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        visit(semanticsResult.getRootNode());
        return semanticsResult;
    }
}
