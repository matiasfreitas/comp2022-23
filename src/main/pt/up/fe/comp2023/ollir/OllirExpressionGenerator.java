package pt.up.fe.comp2023.ollir;


import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;

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
        addVisit("Paren", this::handleParenthesis);
        addVisit("Unary", this::handleUnaryOperator);
        addVisit("NewObject", this::handleNewObject);
        addVisit("NewArray", this::handleNewArray);
        addVisit("ArrayIndexing", this::handleArrayIndexing);
        addVisit("BinaryOp", this::handleBinaryOp);
        addVisit("MethodCalling", this::handleMethodCalling);
        addVisit("AttributeAccessing", this::handleAttributeAccessing);
        addVisit("This", this::handleThis);
        addVisit("Int", this::handleLiteral);
        addVisit("Char", this::handleLiteral);
        addVisit("String", this::handleLiteral);
        addVisit("Boolean", this::handleLiteral);
        addVisit("Identifier", this::handleIdentifier);
    }

    private OllirExpressionResult handleUnaryOperator(JmmNode jmmNode, List<Report> reports) {
        var rhs = visit(jmmNode.getJmmChild(0));
        var op = jmmNode.get("op");
        var resultingType = OllirSymbol.typeFrom(jmmNode);
        var newTemp = new OllirSymbol(nextTemp(), resultingType);
        var assign = ollirAssignment(newTemp, rhs.symbol(), op);
        var code = rhs.code() + assign;
        return new OllirExpressionResult(code, newTemp);
    }


    private OllirExpressionResult handleAttributeAccessing(JmmNode jmmNode, List<Report> reports) {
        // We assume that this is calling array length
        var attributeName = jmmNode.get("attributeName");
        var lhs = visit(jmmNode.getJmmChild(0));
        if (!(attributeName.equals("length") && lhs.symbol().isArray())) {
            System.err.println("Unavailable attribute accessing");
            return new OllirExpressionResult(lhs.code(), OllirSymbol.noSymbol());
        }
        var arrayLength = ollirArrayLength(lhs.symbol());
        return new OllirExpressionResult(lhs.code() + arrayLength.code(), arrayLength.symbol());
    }

    private OllirExpressionResult ollirArrayLength(OllirSymbol array) {
        var value = "arraylength(" + array.toCode() + ")";
        var ollirInt = OllirSymbol.typeFrom(JmmBuiltins.JmmInt);
        var lenCall = new OllirSymbol(value, ollirInt);
        var lenVar = new OllirSymbol(nextTemp(), ollirInt);
        var assignment = ollirAssignment(lenVar, lenCall);
        return new OllirExpressionResult(assignment, lenVar);
    }

    private OllirExpressionResult handleArrayIndexing(JmmNode jmmNode, List<Report> reports) {
        //  expression '[' expression ']' #ArrayIndexing
        var array = visit(jmmNode.getJmmChild(0));
        var index = visit(jmmNode.getJmmChild(1));
        OllirSymbol indexed = ollirArrayIndex(array.symbol(), index.symbol());
        var temp = new OllirSymbol(nextTemp(), indexed.type());
        var assigned = ollirAssignment(temp, indexed);
        return new OllirExpressionResult(array.code() + index.code() + assigned, temp);
    }


    private OllirExpressionResult handleNewArray(JmmNode jmmNode, List<Report> reports) {
        var tg = new TypeGen();
        tg.visit(jmmNode.getJmmChild(0));
        var type = tg.getType();
        var arrayType = new Type(type.getName(), true);
        var ollirType = OllirSymbol.typeFrom(arrayType);
        var ollirSize = visit(jmmNode.getJmmChild(1), reports);
        var ollirArraySymbol = ollirNewArray(ollirSize.symbol(), ollirType);

        return new OllirExpressionResult(ollirSize.code(), ollirArraySymbol);

    }

    private OllirExpressionResult handleNewObject(JmmNode jmmNode, List<Report> reports) {
        var ollirType = OllirSymbol.typeFrom(jmmNode);
        var lhs = new OllirSymbol(nextTemp(), ollirType);
        var rhs = new OllirSymbol("new", ollirType);
        var assignment = ollirAssignment(lhs, rhs);
        var construct = ollirInvokeConstructor(lhs.toCode(), null);
        return new OllirExpressionResult(assignment + construct, lhs);
    }

    private OllirExpressionResult handleParenthesis(JmmNode jmmNode, List<Report> reports) {
        return visit(jmmNode.getJmmChild(0), reports);
    }

    public OllirExpressionResult handleThis(JmmNode jmmNode, List<Report> reports) {
        var type = OllirSymbol.typeFrom(jmmNode);
        return new OllirExpressionResult("", new OllirSymbol("this", type));
    }

    protected OllirExpressionResult handleLiteral(JmmNode jmmNode, List<Report> reports) {
        var literal = OllirSymbol.fromLiteral(jmmNode);
        return new OllirExpressionResult("", literal);

    }

    protected OllirExpressionResult handleFieldIdentifier(JmmNode node, List<Report> reports) {
        var field = fromFieldIdentifier(node);
        return ollirGetField(field, nextTemp());
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
        var type = OllirSymbol.typeFrom(node);
        return new OllirExpressionResult("", new OllirSymbol(node.get("value"), type));
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
        var resultingType = OllirSymbol.typeFrom(jmmNode);
        var lhs = visit(jmmNode.getJmmChild(0), reports);
        var rhs = visit(jmmNode.getJmmChild(1), reports);
        var newTemp = new OllirSymbol(nextTemp(), resultingType);
        var assign = ollirAssignment(newTemp, lhs.symbol(), rhs.symbol(), op);
        var code = lhs.code() + rhs.code() + assign;
        return new OllirExpressionResult(code, newTemp);
    }


    private OllirExpressionResult handleIdentifier(JmmNode node, List<Report> reports) {
        //System.out.println(node.getAttributes());
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

    private OllirExpressionResult handleMethodCalling(JmmNode jmmNode, List<Report> reports) {
        var method = jmmNode.get("methodName");
        var type = OllirSymbol.typeFrom(jmmNode);
        var lhs = visit(jmmNode.getJmmChild(0), reports);
        var code = new StringBuilder(lhs.code());
        var parameters = new ArrayList<OllirSymbol>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            var parameter = visit(jmmNode.getJmmChild(i), reports);
            code.append(parameter.code());
            parameters.add(parameter.symbol());
        }
        OllirExpressionResult invokeRes;
        var nextTemp = (type.equals("V")) ? null : nextTemp();
        if (jmmNode.get("isStatic").equals("true")) {
            invokeRes = ollirInvokeStatic(lhs.symbol(), method, parameters, type, nextTemp);
        } else {
            invokeRes = ollirInvokeVirtual(lhs.symbol(), method, parameters, type, nextTemp);
        }
        return new OllirExpressionResult(code + invokeRes.code(), invokeRes.symbol());
    }

}
