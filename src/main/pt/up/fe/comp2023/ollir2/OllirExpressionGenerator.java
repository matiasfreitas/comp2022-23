package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import java.util.List;

// Thanks Prof. Jõao Bispo
public class OllirExpressionGenerator extends AJmmVisitor<List<Report>,OllirExpressionResult> {
    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
    }

    private OllirExpressionResult defaultVisit(JmmNode jmmNode, List<Report> reports) {
        return new OllirExpressionResult("","");
    }
}
