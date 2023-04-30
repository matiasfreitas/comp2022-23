package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.semantics.UsageContext;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class OllirGenerator extends AOllirGenerator<String> {
    private OllirExpressionGenerator exprGen;

    public OllirGenerator(JmmSymbolTable symbolTable) {
        super(symbolTable);
        this.exprGen = new OllirExpressionGenerator(symbolTable);

    }

    @Override
    protected void buildVisitor() {
        setDefaultVisit(this::defaultVisit);
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("Assignment", this::handleAssignment);
    }


    private String defaultVisit(JmmNode node, List<Report> reports) {
        System.out.println("Visiting node " + node.getKind());
        var code = new StringBuilder();
        for (var child : node.getChildren()) {
            code.append(visit(child, reports));
        }
        return code.toString();
    }

    private String handleImportDeclaration(JmmNode jmmNode, List<Report> reports) {

        return defaultVisit(jmmNode, reports);
    }

    private String handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        //.method public sum(A.array.i32, B.array.i32).array.i32 {
        var signature = jmmNode.get("signature");
        symbolTable.setCurrentMethod(signature);
        String innerCode = defaultVisit(jmmNode, reports);
        symbolTable.setCurrentMethod(null);
        var visibility = "public";
        var modifier = "static";
        var methodName = "ugaBuga";
        Type t = symbolTable.getReturnType(signature);
        String ollirType = OllirSymbol.fromType(t);
        var tokens = Arrays.asList(".method", visibility, modifier, methodName);
        var methodDecl = spaceBetween(tokens);
        List<String> ollirParams = symbolTable.getParameters(signature)
                .stream().map(s -> OllirSymbol.fromSymbol(s).toCode())
                .toList();
        var codeParams = formatArguments(ollirParams);

        return methodDecl + "(" + codeParams + ")." + ollirType + " {\n" + innerCode + "}";
    }

    private String handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        var className = symbolTable.getClassName();
        var parentClass = symbolTable.getSuper();
        var innerCode = defaultVisit(jmmNode, reports);
        String code = className +
                " {\n" +
                innerCode +
                "}";
        return code;
    }


    private String handleAssignment(JmmNode node, List<Report> reports) {
        var idType = IdentifierType.fromJmmNode(node);
        if (idType == null || idType.equals(IdentifierType.ClassType)) {
            System.err.println("This node has no  idType it is not being handled in semantics!!");
            System.out.println(node.toTree());
            return "";
        }
        OllirSymbol lhs = fromIdentifier(node);
        OllirExpressionResult rhs = exprGen.visit(node.getJmmChild(0), reports);
        var code = new StringBuilder(rhs.code());
        if (idType.equals(IdentifierType.ClassField)) {
            code.append(ollirPutField(lhs, rhs.symbol()));

        } else {
            code.append(ollirAssignment(lhs, rhs.symbol()));
        }

        return code.toString();
    }
}
