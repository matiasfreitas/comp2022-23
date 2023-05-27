package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

public class ConstantFolding extends AJmmVisitor<Void, JmmNode> {
    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::noOptimization);
        addVisit("Unary", this::handleUnary);
        addVisit("BinaryOp", this::handleBinaryOp);

    }

    private JmmNode noOptimization(JmmNode jmmNode, Void unused){
        for(var child : jmmNode.getChildren()){
            var foldedChild = visit(child);
            child.replace(foldedChild);
        }
        return jmmNode;
    }
    private JmmNode handleBinaryOp(JmmNode jmmNode, Void unused) {
        System.out.println("Seeing binary operator");
        return jmmNode;
    }

    private JmmNode handleUnary(JmmNode jmmNode, Void unused) {
        System.out.println("Seeing unary operator");
        JmmNode child = jmmNode.getJmmChild(0);
        JmmNode foldedChild = visit(child);
        if (!foldedChild.getKind().equals("Boolean")) {
            child.replace(foldedChild);
            return jmmNode;
        }
        boolean value = child.get("value").equals("true");
        boolean result = !value;
        JmmNode n = new JmmNodeImpl("Boolean");
        n.put("value", String.valueOf(result));
        return n;
    }

    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        JmmNode rootNode = visit(semanticsResult.getRootNode());
        return new JmmSemanticsResult(rootNode,semanticsResult.getSymbolTable(),semanticsResult.getReports(),semanticsResult.getConfig());
    }

}
