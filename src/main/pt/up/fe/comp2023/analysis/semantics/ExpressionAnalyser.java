package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Objects;

public class ExpressionAnalyser extends PostorderJmmVisitor<UsageContext,Type>{

    private JmmNode root;
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
        this.addVisit("PostFix",this::handlePostFix);
        this.addVisit("Unary",this::handleUnary);
        this.addVisit("BinaryOp",this::handleBinaryOp);
        this.addVisit("TernaryOp",this::handleTernaryOp);
        this.addVisit("NewArray",this::handleNewArray);
        this.addVisit("NewObject",this::handleNewObject);
        this.addVisit("Identifier",this::handleIdentifier);

        this.addVisit("This",this::handleThis);


        this.addVisit("Integer",this::handleLiteral);
        this.addVisit("Boolean",this::handleLiteral);
        this.addVisit("CHAR",this::handleLiteral);
        this.addVisit("STRING",this::handleLiteral);

    }

    private Type handleArrayIndexing(JmmNode jmmNode, UsageContext context) {

        JmmNode arrayNode = jmmNode.getJmmChild(0);
        Type arrayType =  this.visit(arrayNode,context);
        if(! arrayType.isArray()){
            // TODO: Add Errror not being Array
            return  null;
        }
        JmmNode indexNode = jmmNode.getJmmChild(1);
        Type indexType = this.visit(indexNode,context);
        if (!indexType.getName().equals("int")){
            // TODO: Add error not index not being  number
            return null;
        }
        return new Type(arrayType.getName(),false);
    }

    private Type handleParen(JmmNode jmmNode, UsageContext context) {
        return this.visit(jmmNode.getJmmChild(0),context);
    }


    private Type handleThis(JmmNode jmmNode, UsageContext context) {
        // Se o contexto for class Declaration
        if(context.isClassContext()){
            // Error
            return  null;
        }
        JmmSymbolTable table = context.getSymbolTable();
        if(table.isStaticMethod(context.getMethodSignature())){
            // Error static cannot have this
            return  null;
        }
        // How to see if method is static?
        // se o contexto for um método estático temos que retornar erro
        // Caso contradio retornamos o tipo da class em que estamos
        String className = table.getClassName();

        return new  Type(className,false);
    }

    private Type handleLiteral(JmmNode jmmNode, UsageContext context) {

        TypeGen typeGen = new TypeGen();
        typeGen.visit(jmmNode);
        return typeGen.getType();
    }

}
