package pt.up.fe.comp2023.analysis.semantics;


import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.BiFunction;

public class ExpressionAnalyser extends PostorderJmmVisitor<List<Report>, Type> {

    private JmmNode root;
    JmmSymbolTable symbolTable;
    private UsageContext context;

    ExpressionAnalyser(JmmNode root, UsageContext context) {
        this.root = root;
        this.context = context;
    }

    public Type type() {
        return null;
    }

    @Override
    protected void buildVisitor() {
        Map<String, BiFunction<JmmNode,List<Report>,Type>> map = new HashMap<>();
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

        map.forEach((k,v) -> {
            this.addVisit(k,this.assignNodeType(v));
        });
    }

    private BiFunction<JmmNode,List<Report>,Type> assignNodeType(BiFunction<JmmNode,List<Report>,Type> function){
        return (JmmNode jmmNode, List<Report> reports) -> {
            Type t = function(jmmNode,reports);
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

    private Type handleIdentifier(JmmNode jmmNode, List<Report> reports) {
        String identifier = jmmNode.get("value");
        Optional<Type> t = Optional.empty();
        if (context.isClassContext()) {
            t = checkUpperScopes(identifier);
        }
        // Method context
        else {
            String currentMethod = context.getMethodSignature();
            for (Symbol s: symbolTable.getParameters(currentMethod)){
                if(s.getName().equals(identifier)){
                    return s.getType();
                }
            }
            for (Symbol s: symbolTable.getLocalVariables(currentMethod)){
                if(s.getName().equals(identifier)){
                    return s.getType();
                }
            }
            t = checkUpperScopes(identifier);
        }
        if(t.isEmpty()){
            // TODO: add errors
            return  null;
        }
        return t.get();
    }


    private Type handleNewObject(JmmNode jmmNode, List<Report> reports) {
        String typeName = jmmNode.get("typeName");
        if (symbolTable.isImportedSymbol(typeName) || symbolTable.isThisClassType(typeName)) {
            return new Type(typeName, false);
        }
        // TODO: Adicionar erro!!!
        return null;

    }

    private Type handleNewArray(JmmNode jmmNode, List<Report> reports) {
        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Type arrayType = this.visit(arrayNode, reports);
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Type indexType = this.visit(indexNode, reports);
        if (!indexType.getName().equals("int")) {
            // TODO: Add error not index not being  number
            return null;
        }
        return new Type(arrayType.getName(), true);

    }

    private Type handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getJmmChild(0);
        Type leftType = this.visit(left, reports);
        JmmNode right = jmmNode.getJmmChild(1);
        Type rightType = this.visit(right, reports);

    }

    private Type handleSingleOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        System.out.println(op);
        Type t = this.visit(jmmNode.getJmmChild(0), reports);
        // TODO: checking
        return t;
    }

    private Type handleAttributeAccessing(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Type objectType = this.visit(object, reports);
        String attributeName = jmmNode.get("attributeName");
        String className = this.symbolTable.getClassName();
        if (objectType.getName().equals(className)) {
            List<Symbol> fields = this.symbolTable.getFields();
            for (Symbol f : fields) {
                if (f.getName().equals(attributeName)) {
                    return f.getType();
                }
            }
            // TODO:  Retornar Erro Class não tem esse methodo
            return null;
        } else {
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return null;
        }

    }

    private Type handleMethodCalling(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Type objectType = this.visit(object, reports);
        String method = jmmNode.get("methodName");
        System.out.println(method);
        List<Type> parameters = new LinkedList<>();
        for (int i = 2; i < jmmNode.getNumChildren(); i++) {
            JmmNode parameter = jmmNode.getJmmChild(i);
            parameters.add(this.visit(parameter, reports));
        }
        // Check if method signature is correct
        String signature = MethodSymbolTable.getStringRepresentation(method, parameters);
        String className = this.symbolTable.getClassName();
        if (objectType.getName().equals(className)) {
            Optional<Type> t = this.symbolTable.getReturnTypeTry(signature);
            if (t.isPresent()) {
                return t.get();
            } else {
                // TODO:  Retornar Erro Class não tem esse methodo
                return null;
            }
        } else {
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return null;
        }

    }

    private Type handleArrayIndexing(JmmNode jmmNode, List<Report> reports) {

        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Type arrayType = this.visit(arrayNode, reports);
        if (!arrayType.isArray()) {
            // TODO: Add Errror not being Array
            return null;
        }
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Type indexType = this.visit(indexNode, reports);
        if (!indexType.getName().equals("int")) {
            // TODO: Add error not index not being  number
            return null;
        }
        return new Type(arrayType.getName(), false);
    }

    private Type handleParen(JmmNode jmmNode, List<Report> reports) {
        return this.visit(jmmNode.getJmmChild(0), reports);
    }


    private Type handleThis(JmmNode jmmNode, List<Report> reports) {
        // Se o contexto for class Declaration
        if (this.context.isClassContext()) {
            // Error
            return null;
        }
        if (this.symbolTable.isStaticMethod(this.context.getMethodSignature())) {
            // Error static cannot have this
            return null;
        }
        // How to see if method is static?
        // se o contexto for um método estático temos que retornar erro
        // Caso contradio retornamos o tipo da class em que estamos
        String className = this.symbolTable.getClassName();

        return new Type(className, false);
    }

    private Type handleLiteral(JmmNode jmmNode, List<Report> reports) {

        TypeGen typeGen = new TypeGen();
        typeGen.visit(jmmNode);
        return typeGen.getType();
    }

}
