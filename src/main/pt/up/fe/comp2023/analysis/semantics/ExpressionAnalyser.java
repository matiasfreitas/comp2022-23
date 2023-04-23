package pt.up.fe.comp2023.analysis.semantics;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.BiFunction;

public class ExpressionAnalyser extends PostorderJmmVisitor<List<Report>, Optional<Type>> {

    private JmmNode root;
    JmmSymbolTable symbolTable;
    private UsageContext context;

    ExpressionAnalyser(JmmNode root, UsageContext context) {
        this.root = root;
        this.context = context;
    }

    @Override
    protected void buildVisitor() {
        Map<String, BiFunction<JmmNode, List<Report>, Optional<Type>>> map = new HashMap<>();
        map.put("Paren", this::handleParen);
        map.put("MethodCalling", this::handleMethodCalling);
        map.put("AttributeAccessing", this::handleAttributeAccessing);
        map.put("ArrayIndexing", this::handleArrayIndexing);
        map.put("PostFix", this::handleSingleOp);
        map.put("Unary", this::handleSingleOp);
        map.put("BinaryOp", this::handleBinaryOp);
        map.put("NewArray", this::handleNewArray);
        map.put("NewObject", this::handleNewObject);
        map.put("Identifier", this::handleIdentifier);
        map.put("This", this::handleThis);
        map.put("Integer", this::handleLiteral);
        map.put("Boolean", this::handleLiteral);
        map.put("CHAR", this::handleLiteral);
        map.put("STRING", this::handleLiteral);

        map.forEach((k, v) -> {
            this.addVisit(k, this.assignNodeType(v));
        });
    }
    private Report createReport(JmmNode node,String message){
        int line = Integer.parseInt(node.get("LINE"));
        int column  = Integer.parseInt(node.get("COLUMN"));
        return  Report.newError(Stage.SEMANTIC,line,column,message,null);

    }
    private BiFunction<JmmNode, List<Report>, Optional<Type>> assignNodeType(BiFunction<JmmNode, List<Report>, Optional<Type>> function) {
        return (JmmNode jmmNode, List<Report> reports) -> {
            Optional<Type> t = function.apply(jmmNode, reports);
            return t;
        };
    }

    private Optional<Type> checkUpperScopes(String identifier) {
        Optional<Type> classField = symbolTable.getFieldTry(identifier);
        if (classField.isEmpty()) {
            if (symbolTable.isImportedSymbol(identifier)) {
                return Optional.of(new Type(identifier, false));
            }
            // TODO: error undefined identifier
        }
        return classField;
    }

    private Optional<Type> handleIdentifier(JmmNode jmmNode, List<Report> reports) {
        String identifier = jmmNode.get("value");
        Optional<Type> t = Optional.empty();
        if (context.isClassContext()) {
            t = checkUpperScopes(identifier);
        }
        // Method context
        else {
            String currentMethod = context.getMethodSignature();
            for (Symbol s : symbolTable.getParameters(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            for (Symbol s : symbolTable.getLocalVariables(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            t = checkUpperScopes(identifier);
        }
        if (t.isEmpty()) {
            // TODO: add errors
        }
        return t;
    }


    private Optional<Type> handleNewObject(JmmNode jmmNode, List<Report> reports) {
        String typeName = jmmNode.get("typeName");
        if (symbolTable.isImportedSymbol(typeName) || symbolTable.isThisClassType(typeName)) {
            return Optional.of(new Type(typeName, false));
        }
        // TODO: Adicionar erro!!!
        return Optional.empty();

    }

    private Optional<Type> handleNewArray(JmmNode jmmNode, List<Report> reports) {
        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Optional<Type> arrayType = this.visit(arrayNode, reports);
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Optional<Type> indexType = this.visit(indexNode, reports);
        if(arrayType.isEmpty() || indexType.isEmpty()){
            return   Optional.empty();
        }
        if (indexType.get().getName().equals("int")) {
            // TODO: Add error not index not being  number
            return Optional.empty();
        }
        return Optional.of(new Type(arrayType.get().getName(), true));

    }

    private Optional<Type> handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getJmmChild(0);
        Optional<Type> leftType = this.visit(left, reports);
        JmmNode right = jmmNode.getJmmChild(1);
        Optional<Type> rightType= this.visit(right, reports);
        return leftType;

    }

    private Optional<Type> handleSingleOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        System.out.println(op);
        Optional<Type> t = this.visit(jmmNode.getJmmChild(0), reports);
        // TODO: checking
        return t;
    }

    private Optional<Type> handleAttributeAccessing(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Optional<Type> objectType = this.visit(object, reports);
        String attributeName = jmmNode.get("attributeName");
        if (this.symbolTable.isThisClassType(objectType.get().getName())){
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
        Optional<Type> objectType = this.visit(object, reports);
        if(objectType.isEmpty()){
            //Probably will not do nothing?
        }
        String method = jmmNode.get("methodName");
        System.out.println(method);
        List<Type> parameters = new LinkedList<>();
        for (int i = 2; i < jmmNode.getNumChildren(); i++) {
            JmmNode parameter = jmmNode.getJmmChild(i);
            Optional<Type> parameterType = this.visit(parameter, reports);
            if(parameterType.isEmpty()){
                // write errorr
                // Show possible overloads for methods?
            }
            else{
                parameters.add(parameterType.get());
            }

        }
        // Check if method signature is correct
        String signature = MethodSymbolTable.getStringRepresentation(method, parameters);
        if (this.symbolTable.isThisClassType(objectType.get().getName())) {
            Optional<Type> t = this.symbolTable.getReturnTypeTry(signature);
            if(t.isEmpty()){
                // add error
            }
            return t;
        } else {
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return Optional.empty();
        }

    }

    private Optional<Type> handleArrayIndexing(JmmNode jmmNode, List<Report> reports) {

        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Optional<Type> arrayType = this.visit(arrayNode, reports);
        if (arrayType.isEmpty() || !arrayType.get().isArray()) {
            // TODO: Add Errror not being Array
            return Optional.empty();
        }
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Optional<Type> indexType = this.visit(indexNode, reports);
        if (indexType.isEmpty() || !indexType.get().getName().equals("int")) {
            // TODO: Add error not index not being  number
            return Optional.empty();
        }
        return Optional.of(new Type(arrayType.get().getName(), false));
    }

    private Optional<Type> handleParen(JmmNode jmmNode, List<Report> reports) {
        return this.visit(jmmNode.getJmmChild(0), reports);
    }


    private Optional<Type> handleThis(JmmNode jmmNode, List<Report> reports) {
        // Se o contexto for class Declaration
        if (this.context.isClassContext()) {
            // Error
            return Optional.empty();
        }
        if (this.symbolTable.isStaticMethod(this.context.getMethodSignature())) {
            // Error static cannot have this
            return Optional.empty();
        }
        // How to see if method is static?
        // se o contexto for um método estático temos que retornar erro
        // Caso contradio retornamos o tipo da class em que estamos
        String className = this.symbolTable.getClassName();

        return Optional.of(new Type(className, false));
    }

    private Optional<Type> handleLiteral(JmmNode jmmNode, List<Report> reports) {

        TypeGen typeGen = new TypeGen();
        typeGen.visit(jmmNode);
        return Optional.ofNullable(typeGen.getType());
    }

}
