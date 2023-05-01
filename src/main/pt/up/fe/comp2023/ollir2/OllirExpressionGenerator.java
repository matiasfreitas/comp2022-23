package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.semantics.UsageContext;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

// Thanks Prof. Jõao Bispo
public class OllirExpressionGenerator extends AOllirGenerator<OllirExpressionResult> {

    private int tempCounter = 0;

    private JmmSymbolTable symbolTable;

    public OllirExpressionGenerator(JmmSymbolTable symbolTable) {
        super(symbolTable);
    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("BinaryOp", this::handleBinaryOp);
        addVisit("Int", this::handleLiteral);
        addVisit("Char", this::handleLiteral);
        addVisit("String", this::handleLiteral);
        addVisit("Boolean", this::handleLiteral);
        addVisit("Identifier", this::handleIdentifier);
    }

    protected OllirExpressionResult handleLiteral(JmmNode jmmNode, List<Report> reports) {
        var literal = OllirSymbol.fromLiteral(jmmNode);
        return new OllirExpressionResult("", literal);

    }

    protected OllirExpressionResult handleFieldIdentifier(JmmNode node, List<Report> reports) {
        var field = fromFieldIdentifier(node);
        return new OllirExpressionResult("", ollirGetField(field));
    }

    private OllirExpressionResult handleLocalVariable(JmmNode node, List<Report> reports) {
        var local = fromLocalVariable(node);
        return new OllirExpressionResult("", local);
    }

    private OllirExpressionResult handleParameterIdentifier(JmmNode node, List<Report> reports) {
        var parameter = fromParameterIdentifier(node);
        return new OllirExpressionResult("", parameter);
    }

    private OllirExpressionResult handleClassType(JmmNode node, List<Report> reports) {
        // TODO:
        return new OllirExpressionResult("", OllirSymbol.noSymbol());
    }

    public String nextTemp() {
        String nextTemp = "temp_" + tempCounter;
        tempCounter++;
        return nextTemp;
    }


    private OllirExpressionResult defaultVisit(JmmNode jmmNode, List<Report> reports) {
        return new OllirExpressionResult("", OllirSymbol.noSymbol());
    }


    private OllirExpressionResult handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        var op = jmmNode.get("op");
        var lhs = visit(jmmNode.getJmmChild(0), reports);
        var rhs = visit(jmmNode.getJmmChild(1), reports);
        // This assumes type checking was already done
        var newTemp = new OllirSymbol(nextTemp(), lhs.symbol().type());
        var operation = newTemp.toCode() + " :=." + lhs.symbol().type() + " " + lhs.symbol().toCode() + " " + op + "." + lhs.symbol().type() + " " + rhs.symbol().toCode() + ";\n";
        var code = new StringBuilder(lhs.code());
        code.append(rhs.code())
                .append(operation);
        return new OllirExpressionResult(code.toString(), newTemp);
    }


    private OllirExpressionResult handleIdentifier(JmmNode node, List<Report> reports) {
        System.out.println(node.getAttributes());
        IdentifierType idType = IdentifierType.fromJmmNode(node);
        if (idType == null) {
            System.err.println("This node has no  idType it is not being handled in semantics!!");
            System.out.println(node.toTree());
            return new OllirExpressionResult("", OllirSymbol.noSymbol());
        }
        return switch (idType) {
            case ClassField -> handleFieldIdentifier(node, reports);
            case MethodParameter -> handleParameterIdentifier(node, reports);
            case LocalVariable -> handleLocalVariable(node, reports);
            case ClassType -> handleClassType(node, reports);
        };
    }

}
