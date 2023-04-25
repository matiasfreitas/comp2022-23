package pt.up.fe.comp2023.analysis.semantics;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import javax.swing.text.html.Option;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;

public class ExpressionAnalyser extends Analyser<Optional<Type>> {

    private Optional<Type> type = Optional.empty();

    ExpressionAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root, symbolTable, context);
    }


    @Override
    protected void buildVisitor() {
        Map<String, BiFunction<JmmNode, List<Report>, Optional<Type>>> map = new HashMap<>();
        map.put("Paren", this::handleParen);
        map.put("MethodCalling", this::handleMethodCalling);
        map.put("AttributeAccessing", this::handleAttributeAccessing);
        map.put("ArrayIndexing", this::handleArrayIndexing);
        map.put("Unary", this::handleSingleOp);
        map.put("BinaryOp", this::handleBinaryOp);
        map.put("NewArray", this::handleNewArray);
        map.put("NewObject", this::handleNewObject);
        map.put("Identifier", this::handleIdentifier);
        map.put("This", this::handleThis);
        map.put("Int", this::handleLiteral);
        map.put("Boolean", this::handleLiteral);
        map.put("Char", this::handleLiteral);
        map.put("String", this::handleLiteral);

        map.forEach((k, v) -> {
            this.addVisit(k, this.assignNodeType(v));
        });
        this.setDefaultVisit((a,b) -> Optional.empty());
    }


    private BiFunction<JmmNode, List<Report>, Optional<Type>> assignNodeType(BiFunction<JmmNode, List<Report>, Optional<Type>> function) {
        return (JmmNode jmmNode, List<Report> reports) -> {
            //System.out.println("Node " + jmmNode.getKind());
            Optional<Type> maybeT = function.apply(jmmNode, reports);
            if (maybeT.isPresent()) {
                Type t = maybeT.get();
                jmmNode.put("type", t.getName());
                jmmNode.put("isArray", (t.isArray()) ? "true" : "false");
            }
            return maybeT;
        };
    }


    private Optional<Type> handleIdentifier(JmmNode jmmNode, List<Report> reports) {
        System.out.println("Im checking identifier "+ jmmNode.get("value"));
        return this.checkIdentifier(jmmNode.get("value"), jmmNode, reports);
    }


    private Optional<Type> handleNewObject(JmmNode jmmNode, List<Report> reports) {
        String typeName = jmmNode.get("typeName");
        if (symbolTable.isImportedSymbol(typeName) || symbolTable.isThisClassType(typeName)) {
            return Optional.of(new Type(typeName, false));
        }
        return Optional.empty();

    }

    private Optional<Type> handleNewArray(JmmNode jmmNode, List<Report> reports) {
        JmmNode typeNode = jmmNode.getJmmChild(0);
        TypeGen typeGen = new TypeGen();
        typeGen.visit(typeNode);
        Type arrayType = typeGen.getType();
        // TODO: availableType is always true
        boolean availableType = true;
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Optional<Type> indexType = this.visit(indexNode, reports);
        if (availableType || indexType.isEmpty()) {
            return Optional.empty();
        }
        if (!indexType.get().getName().equals("int")) {
            reports.add(this.createReport(jmmNode, "Index of an Array Must be an integer got: " + indexType.toString()));
            return Optional.empty();
        }
        return Optional.of(new Type(arrayType.getName(), true));

    }

    private Optional<Type> handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getJmmChild(0);
        Optional<Type> maybeLeftType = this.visit(left, reports);
        JmmNode right = jmmNode.getJmmChild(1);
        Optional<Type> maybeRightType = this.visit(right, reports);
        if (maybeRightType.isPresent() && maybeLeftType.isPresent()) {
            Type rightType = maybeRightType.get();
            Type leftType = maybeLeftType.get();
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/") || op.equals("<")) {
                if (rightType.equals(leftType) && rightType.equals(JmmBuiltins.JmmInt)) {
                    return Optional.of(leftType);
                }
            } else if (op.equals("&&")) {
                if (rightType.equals(leftType) && rightType.equals(JmmBuiltins.JmmBoolean)) {
                    return Optional.of(leftType);
                }
            }
            reports.add(this.createReport(jmmNode, op + " operator expects int" + op + " int got:" + leftType.toString() + " " + op + " " + rightType.toString()));
        }
        return Optional.empty();

    }

    private Optional<Type> handleSingleOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        //System.out.println(jmmNode.getAttributes());
        Optional<Type> maybeT = this.visit(jmmNode.getJmmChild(0), reports);
        if (maybeT.isPresent()) {
            Type t = maybeT.get();
            if (op.equals("!") && t.equals(JmmBuiltins.JmmBoolean)) {
                return Optional.of(t);
            }
            reports.add(this.createReport(jmmNode, op + " operator expects " + op + "boolean got:!" + t.toString()));
        }
        return Optional.empty();
    }

    private Optional<Type> handleAttributeAccessing(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Optional<Type> maybeObjectType = this.visit(object, reports);
        if (maybeObjectType.isEmpty()) {
            return maybeObjectType;
        }
        Type objectType = maybeObjectType.get();
        String attributeName = jmmNode.get("attributeName");
        if (this.symbolTable.isThisClassType(objectType.getName())) {
            List<Symbol> fields = this.symbolTable.getFields();
            for (Symbol f : fields) {
                if (f.getName().equals(attributeName)) {
                    return Optional.ofNullable(f.getType());
                }
            }
            // TODO:  Retornar Erro Class não tem esse methodo
            return Optional.empty();
        } else {
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return Optional.empty();
        }

    }

    private Optional<Type> handleMethodCalling(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Optional<Type> maybeObjectType = this.visit(object, reports);
        // Undefined Object
        if (maybeObjectType.isEmpty()) {
            // Não faz sentido continuar a checkar?
            return Optional.empty();
        }
        Type objectType = maybeObjectType.get();
        boolean error = false;
        String method = jmmNode.get("methodName");
        List<Type> parameters = new LinkedList<>();
        for (int i = 1; i < jmmNode.getNumChildren(); i++) {
            JmmNode parameter = jmmNode.getJmmChild(i);
            Optional<Type> parameterType = this.visit(parameter, reports);
            if (parameterType.isEmpty()) {
                // show possible overloads
            } else {
                parameters.add(parameterType.get());
            }

        }
        String signature = MethodSymbolTable.getStringRepresentation(method, parameters);
        //System.out.println(signature);
        if (this.symbolTable.isThisClassType(objectType.getName())) {
            Optional<Type> t = this.symbolTable.getReturnTypeTry(signature);
            // The class we are defining does not contain that method
            if (t.isEmpty()) {
                // Check if it extends an imported class if so assume it is correct
                String superClass = this.symbolTable.getSuper();
                if (this.symbolTable.isImportedSymbol(superClass)) {
                    return Optional.of(JmmBuiltins.JmmAssumeType);
                }
                reports.add(this.createReport(jmmNode, "Is Not an available Method"));
                List<MethodSymbolTable> similars = this.symbolTable.getOverloads(method);
                if (similars.size() > 0) {
                    String message = this.createOverloadReports(method, parameters, similars);
                    reports.add(this.createReport(jmmNode, message));
                }
            }
            return t;
        } else {
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return Optional.empty();
        }

    }

    private String createOverloadReports(String method, List<Type> parameters, List<MethodSymbolTable> similars) {

        StringBuilder s = new StringBuilder("Your Method: \n");
        s.append(
                this.methodString(
                        method,
                        (List<String>) parameters.stream().map(type -> type.toString())
                )
        );
        s.append("\n");
        if (similars.size() == 1) {
            s.append("Did you Mean: \n");

        } else {
            s.append("Possible Overloads: \n");
        }
        for (MethodSymbolTable mt : similars) {
            String mtString = this.methodString(mt.getName(), (List<String>) mt.getParameters().stream().map((ms) -> s.toString()));
            s.append(mtString);
            s.append("\n");
        }

        return s.toString();

    }

    private String methodString(String method, List<String> parameters) {
        StringBuilder s = new StringBuilder(method);
        s.append('(');
        int i = 0;
        for (; i < parameters.size() - 1; i++) {
            s.append(parameters.get(i));
            s.append(", ");
        }
        if (i < parameters.size())
            s.append(parameters.get(i));
        s.append(')');
        return s.toString();
    }

    private Optional<Type> handleArrayIndexing(JmmNode jmmNode, List<Report> reports) {

        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Optional<Type> arrayType = this.visit(arrayNode, reports);
        boolean error = false;
        if (arrayType.isEmpty() || !arrayType.get().isArray()) {
            reports.add(this.createReport(jmmNode, "Trying To Index over a type that is not an array"));
            error = true;
        }
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Optional<Type> indexType = this.visit(indexNode, reports);
        if (indexType.isPresent() && !indexType.get().getName().equals("int")) {
            reports.add(this.createReport(jmmNode, "Index of an Array Must be an integer got: " + indexType.get().toString()));
            error = true;
        }
        if (error) {
            return Optional.empty();
        }
        return Optional.of(new Type(arrayType.get().getName(), false));
    }

    private Optional<Type> handleParen(JmmNode jmmNode, List<Report> reports) {
        return this.visit(jmmNode.getJmmChild(0), reports);
    }


    private Optional<Type> handleThis(JmmNode jmmNode, List<Report> reports) {
        if (this.context.isClassContext()) {
            reports.add(this.createReport(jmmNode, "Usage Of `this` in class fields is not allowed"));
            return Optional.empty();
        }
        if (this.symbolTable.isStaticMethod(this.context.getMethodSignature())) {
            reports.add(this.createReport(jmmNode, "Usage Of `this` in static method is not allowed"));
            return Optional.empty();
        }
        String className = this.symbolTable.getClassName();
        return Optional.of(new Type(className, false));
    }

    private Optional<Type> handleLiteral(JmmNode jmmNode, List<Report> reports) {
        Optional<Type> builtin = JmmBuiltins.fromJmmNode(jmmNode);
        return builtin;
    }

    @Override
    public List<Report> analyse() {
        List<Report> reports = new LinkedList<>();
        this.type = this.visit(this.root, reports);
        return reports;
    }

    public Optional<Type> getType() {
        return this.type;
    }

}
