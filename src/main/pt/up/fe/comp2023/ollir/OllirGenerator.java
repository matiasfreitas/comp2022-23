package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OllirGenerator extends AOllirGenerator<String> {
    private OllirExpressionGenerator exprGen;

    private LabelPair labelIf;
    private LabelPair labelWhile;

    public OllirGenerator(JmmSymbolTable symbolTable) {
        super(symbolTable);
        this.exprGen = new OllirExpressionGenerator(symbolTable);
        this.labelIf = new LabelPair("if", "enter", "end");
        this.labelWhile = new LabelPair("while", "condition", "body");

    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("ClassVarDeclaration", this::handleClassField);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("Assignment", this::handleAssignment);
        addVisit("ArrayAssignment", this::handleArrayAssignment);
        addVisit("SingleStatement", this::handleSingleStatement);
        addVisit("ReturnStatement", this::handleReturn);
        addVisit("IfStatement", this::handleIfStatement);
        addVisit("WhileLoop", this::handleWhileStatement);
    }

    private String handleWhileStatement(JmmNode jmmNode, List<Report> reports) {
        var condition = exprGen.visit(jmmNode.getJmmChild(0));
        var whileBlock = visit(jmmNode.getJmmChild(1));

        var enterBody = labelWhile.second();
        var enterCondition = labelWhile.first();
        labelWhile.next();
        return ollirGoTo(enterCondition) + ollirLabel(enterBody) + whileBlock + ollirLabel(enterCondition) + condition.code() + "if(" + condition.symbol().toCode() + ") " + ollirGoTo(enterBody);
    }

    private String handleIfStatement(JmmNode jmmNode, List<Report> reports) {
        var condition = exprGen.visit(jmmNode.getJmmChild(0));

        var ifBlock = visit(jmmNode.getJmmChild(1));
        var elseBlock = visit(jmmNode.getJmmChild(2));

        var enterIf = labelIf.first();
        var endIf = labelIf.second();
        labelIf.next();
        return condition.code() + "if(" + condition.symbol().toCode() + ") " + ollirGoTo(enterIf) + elseBlock + ollirGoTo(endIf) + ollirLabel(enterIf) + ifBlock + ollirLabel(endIf);

    }

    private String ollirGoTo(String label) {
        return "goto " + label + ";\n";
    }

    private String ollirLabel(String label) {
        return label + ":\n";
    }

    private String handleArrayAssignment(JmmNode jmmNode, List<Report> reports) {
        var index = exprGen.visit(jmmNode.getJmmChild(0));
        var value = exprGen.visit(jmmNode.getJmmChild(1));
        var array = jmmNode.get("varName");
        var arrayAssignment = ollirArrayAssignment(array, index.symbol(), value.symbol());
        return index.code() + value.code() + arrayAssignment;
    }


    private String defaultVisit(JmmNode node, List<Report> reports) {
        //System.out.println("Visiting node " + node.getKind());
        var code = new StringBuilder();
        for (var child : node.getChildren()) {
            code.append(visit(child, reports));
        }
        return code.toString();
    }

    private String handleSingleStatement(JmmNode jmmNode, List<Report> reports) {
        var expr = exprGen.visit(jmmNode.getJmmChild(0));
        return expr.code();
    }

    private String handleReturn(JmmNode jmmNode, List<Report> reports) {
        var toReturn = exprGen.visit(jmmNode.getJmmChild(0));
        return toReturn.code() + "ret." + toReturn.symbol().type() + " " + toReturn.symbol().toCode() + ";\n";

    }

    private String handleImportDeclaration(JmmNode jmmNode, List<Report> reports) {
        return "import " + jmmNode.get("fullModule") + ";\n";

    }

    private String handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        //.method public sum(A.array.i32, B.array.i32).array.i32 {
        var signature = jmmNode.get("signature");
        symbolTable.setCurrentMethod(signature);
        String innerCode = defaultVisit(jmmNode, reports);
        symbolTable.setCurrentMethod(null);
        var visibility = symbolTable.getMethodVisibility(signature);
        var modifier = symbolTable.isStaticMethod(signature) ? "static" : "";
        var methodName = jmmNode.get("methodName");
        Type t = symbolTable.getReturnType(signature);
        var retV = (t.equals(JmmBuiltins.JmmVoid)) ? "ret.V;\n" : "";
        String ollirType = OllirSymbol.typeFrom(t);
        var tokens = Arrays.asList(".method", visibility, modifier, methodName);
        var methodDecl = spaceBetween(tokens);
        List<String> ollirParams = symbolTable.getParameters(signature).stream().map(s -> OllirSymbol.fromSymbol(s).toCode()).toList();
        var codeParams = formatArguments(ollirParams);

        return methodDecl + "(" + codeParams + ")." + ollirType + " {\n" + innerCode + retV + "}\n";
    }

    private String ollirConstructor() {
        return ".construct " + symbolTable.getClassName() + "().V {\n" + ollirInvokeConstructor("this", null) + "}\n";
    }

    private String handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        var className = symbolTable.getClassName();
        var parentClass = symbolTable.getSuper();
        var extendsParent = (parentClass == null) ? "" : " extends " + parentClass;
        var fields = new ArrayList<String>();
        var methods = new ArrayList<String>();
        for (var child : jmmNode.getChildren()) {
            var childCode = visit(child, reports);
            if (child.getKind().equals("ClassVarDeclaration")) {
                fields.add(childCode);
            } else {
                methods.add(childCode);
            }
        }
        return className + extendsParent + " {\n" + String.join("", fields) + ollirConstructor() + String.join("", methods) + "}";
    }

    private boolean iincOptimizable(JmmNode jmmNode) {
        if (!jmmNode.getKind().equals("BinaryOp")) {
            return false;
        }
        var lhs = jmmNode.getJmmChild(0);
        var rhs = jmmNode.getJmmChild(1);
        return JmmBuiltins.iincOptimizable(lhs) && JmmBuiltins.iincOptimizable(rhs);

    }

    public String iincOptimized(OllirSymbol symbol, JmmNode binaryOp) {
        var lhs = exprGen.visit(binaryOp.getJmmChild(0));
        var rhs = exprGen.visit(binaryOp.getJmmChild(1));
        return ollirAssignment(symbol, lhs.symbol(), rhs.symbol(), binaryOp.get("op"));
    }

    private String handleAssignment(JmmNode node, List<Report> reports) {
        var idType = IdentifierType.fromJmmNode(node);
        if (idType == null || idType.equals(IdentifierType.ClassType)) {
            System.err.println("This node has no  idType it is not being handled in semantics!!");
            //System.out.println(node.toTree());
            return "";
        }
        OllirSymbol lhs = fromIdentifier(node);
        if (iincOptimizable(node.getJmmChild(0))) {
            return iincOptimized(lhs, node.getJmmChild(0));
        }
        OllirExpressionResult rhs = exprGen.visit(node.getJmmChild(0), reports);
        var code = new StringBuilder(rhs.code());
        if (idType.equals(IdentifierType.ClassField)) {
            code.append(ollirPutField(lhs, rhs.symbol()));

        } else {
            code.append(ollirAssignment(lhs, rhs.symbol()));
        }

        return code.toString();
    }

    private String handleClassField(JmmNode jmmNode, List<Report> reports) {
        var visibility = jmmNode.hasAttribute("visibility") ? jmmNode.get("visibility") : "private";
        var symbolGen = new SymbolGen();
        symbolGen.visit(jmmNode.getJmmChild(0));
        var symbol = symbolGen.getSymbol();
        var ollirSymbol = OllirSymbol.fromSymbol(symbol);
        return ".field " + visibility + " " + ollirSymbol.toCode() + ";\n";
    }
}
