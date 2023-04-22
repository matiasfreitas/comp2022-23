package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ExpressionAnalyser extends PostorderJmmVisitor<List<Report>,Type>{

    private JmmNode root;
    JmmSymbolTable symbolTable;
    private UsageContext context;
    ExpressionAnalyser(JmmNode root,UsageContext context){
        this.root = root;
        this.context = context;

    }
    public Type type(){
        return null;
    }

    @Override
    protected void buildVisitor() {
        this.addVisit("Paren",this::handleParen);
        this.addVisit("MethodCalling",this::handleMethodCalling);
        this.addVisit("AttributeAccessing",this::handleAttributeAccessing);
        this.addVisit("ArrayIndexing",this::handleArrayIndexing);
        this.addVisit("PostFix",this::handleSingleOp);
        this.addVisit("Unary",this::handleSingleOp);
        this.addVisit("BinaryOp",this::handleBinaryOp);
        //this.addVisit("TernaryOp",this::handleTernaryOp);
        this.addVisit("NewArray",this::handleNewArray);
        this.addVisit("NewObject",this::handleNewObject);
        this.addVisit("Identifier",this::handleIdentifier);

        this.addVisit("This",this::handleThis);


        this.addVisit("Integer",this::handleLiteral);
        this.addVisit("Boolean",this::handleLiteral);
        this.addVisit("CHAR",this::handleLiteral);
        this.addVisit("STRING",this::handleLiteral);

    }

    private Type handleBinaryOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        JmmNode left = jmmNode.getJmmChild(0);
        Type leftType = this.visit(left,reports);
        JmmNode right = jmmNode.getJmmChild(1);
        Type rightType = this.visit(right,reports);

    }

    private Type handleSingleOp(JmmNode jmmNode, List<Report> reports) {
        String op = jmmNode.get("op");
        System.out.println(op);
        Type t = this.visit(jmmNode.getJmmChild(0),reports);
        // TODO: checking
        return t;
    }

    private Type handleAttributeAccessing(JmmNode jmmNode, List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Type objectType = this.visit(object,reports);
        String attributeName= jmmNode.get("attributeName");
        String className = this.symbolTable.getClassName();
        if(objectType.getName().equals(className)){
            List<Symbol> fields = this.symbolTable.getFields();
            for (Symbol f:fields) {
                if(f.getName().equals(attributeName)){
                    return f.getType();
                }
            }
            // TODO:  Retornar Erro Class não tem esse methodo
            return null;
            }
        else{
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return  null;
        }

    }

    private Type handleMethodCalling(JmmNode jmmNode,List<Report> reports) {
        JmmNode object = jmmNode.getJmmChild(0);
        Type objectType = this.visit(object,reports);
        String method = jmmNode.get("methodName");
        System.out.println(method);
        List<Type> parameters = new LinkedList<>();
        for(int i=2; i< jmmNode.getNumChildren(); i++){
            JmmNode parameter = jmmNode.getJmmChild(i);
            parameters.add(this.visit(parameter,reports));
        }
        // Check if method signature is correct
        String signature = MethodSymbolTable.getStringRepresentation(method,parameters);
        String className = this.symbolTable.getClassName();
        if(objectType.getName().equals(className)){
            Optional<Type> t = this.symbolTable.getReturnTypeTry(signature);
            if(t.isPresent()){
                return t.get();
            }
            else{
                // TODO:  Retornar Erro Class não tem esse methodo
                return null;
            }
        }
        else{
            // TODO:  Verificar que object é um import
            // Se não for  retornar erro
            return  null;
        }

    }

    private Type handleArrayIndexing(JmmNode jmmNode,List<Report> reports) {

        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Type arrayType =  this.visit(arrayNode,reports);
        if(! arrayType.isArray()){
            // TODO: Add Errror not being Array
            return  null;
        }
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Type indexType = this.visit(indexNode,reports);
        if (!indexType.getName().equals("int")){
            // TODO: Add error not index not being  number
            return null;
        }
        return new Type(arrayType.getName(),false);
    }

    private Type handleParen(JmmNode jmmNode, List<Report> reports) {
        return this.visit(jmmNode.getJmmChild(0),reports);
    }


    private Type handleThis(JmmNode jmmNode, List<Report>reports) {
        // Se o contexto for class Declaration
        if(this.context.isClassContext()){
            // Error
            return  null;
        }
        if(this.symbolTable.isStaticMethod(this.context.getMethodSignature())){
            // Error static cannot have this
            return  null;
        }
        // How to see if method is static?
        // se o contexto for um método estático temos que retornar erro
        // Caso contradio retornamos o tipo da class em que estamos
        String className = this.symbolTable.getClassName();

        return new  Type(className,false);
    }

    private Type handleLiteral(JmmNode jmmNode, List<Report> reports) {

        TypeGen typeGen = new TypeGen();
        typeGen.visit(jmmNode);
        return typeGen.getType();
    }

}
