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
public class OllirExpressionGenerator extends AJmmVisitor<List<Report>, OllirExpressionResult> {

    private int tempCounter = 0;

    private JmmSymbolTable symbolTable;

    public OllirExpressionGenerator(JmmSymbolTable symbolTable) {
        this.symbolTable = symbolTable;
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
        var lhs = visit(jmmNode.getJmmChild(0));
        var rhs = visit(jmmNode.getJmmChild(1));
        // This assumes type checking was already done
        var newTemp = new OllirSymbol(nextTemp(), lhs.symbol().type());
        var operation = newTemp.toCode() + " :=." + lhs.symbol().type() + " " + lhs.symbol().toCode() + " " + op + "." + lhs.symbol().type() + " " + rhs.symbol().toCode() + ";\n";
        var code = new StringBuilder(lhs.code());
        code.append(rhs.code())
                .append(operation);
        return new OllirExpressionResult(code.toString(), newTemp);
    }

    private OllirExpressionResult handleLiteral(JmmNode jmmNode, List<Report> reports) {
        var symbol = OllirSymbol.fromLiteral(jmmNode);
        return new OllirExpressionResult("", symbol);
    }

    private OllirExpressionResult handleFieldIdentifier(JmmNode node, List<Report> reports) {
        // getfield(this, a.i32).i32; -> this is also a ollir symbol
        var field = symbolTable.getFieldTry(node.get("value"));
        if(field.isEmpty()){
            // this can't happen
            return new OllirExpressionResult("", OllirSymbol.noSymbol());
        }
        var ollirField = OllirSymbol.fromSymbol(field.get());
        return new OllirExpressionResult("",ollirField.getField());
    }

    private OllirExpressionResult handleLocalVariable(JmmNode node, List<Report> reports) {
        String currentMethod = symbolTable.getCurrentMethod();
        Optional<Symbol> local = symbolTable.getLocalVariableTry(currentMethod, node.get("value"));
        // Isto quase de certeza que não vai acontecer o que devo fazer?
        if (local.isEmpty())
            return new OllirExpressionResult("", OllirSymbol.noSymbol());

        return new OllirExpressionResult("", OllirSymbol.fromSymbol(local.get()));

    }

    private OllirExpressionResult handleParameterIdentifier(JmmNode node, List<Report> reports) {
        String currentMethod = symbolTable.getCurrentMethod();
        String identifier = node.get("value");
        var arguments = symbolTable.getParameters(currentMethod);
        int i = 0;
        for (; i < arguments.size(); i++) {
            if (arguments.get(i).getName().equals(identifier)) {
                break;
            }
        }
        Symbol parameter = arguments.get(i);
        String ollirType = OllirSymbol.fromType(parameter.getType());
        if(!symbolTable.isStaticMethod(currentMethod)){
            // The first argument is this so we increment i
           i++;
        }
        OllirSymbol ollirSymbol = new OllirSymbol("$" + i + "." + parameter.getName(), ollirType);
        return new OllirExpressionResult("", ollirSymbol);
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

    private OllirExpressionResult handleClassType(JmmNode node, List<Report> reports) {
        return new OllirExpressionResult("", OllirSymbol.noSymbol());
    }
}
