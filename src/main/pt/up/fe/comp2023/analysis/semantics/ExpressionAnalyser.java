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

