package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

// Thanks Prof. Jõao Bispo
public class OllirExpressionGenerator extends AJmmVisitor<List<Report>, OllirExpressionResult> {

    private int tempCounter = 0;
    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("BinaryOp", this::handleBinaryOp);
        addVisit("Int",this::handleLiteral);
        addVisit("Char",this::handleLiteral);
        addVisit("String",this::handleLiteral);
        addVisit("Boolean",this::handleLiteral);
    }

    public String nextTemp(){
        String nextTemp= "temp_" + tempCounter;
        tempCounter++;
        return nextTemp;
    }


    private OllirExpressionResult defaultVisit(JmmNode jmmNode, List<Report> reports) {
        return new OllirExpressionResult("", "");
    }

    private OllirExpressionResult handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        var op = jmmNode.get("op");
        var lhs = visit(jmmNode.getJmmChild(0));
        var rhs = visit(jmmNode.getJmmChild(1));
        var newTemp= nextTemp();
        var operation = newTemp + ":=" + lhs.value() + op + rhs.value() + ";\n";
        var code = new StringBuilder(lhs.code());
        code.append(rhs.code())
                .append(operation);
        return new OllirExpressionResult(code.toString(),newTemp);
    }

    private OllirExpressionResult handleLiteral(JmmNode jmmNode, List<Report> reports) {

         var symbol = OllirSymbol.fromLiteral(jmmNode);
         return new OllirExpressionResult("",symbol.value());
    }
}
