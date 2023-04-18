package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;
/*
importDeclaration
classDeclaration

methodDeclaration
methodBody

arrayType
simpleType

varTypeSpecification
varDeclaration
methodArguments
classVarDeclaration

ScopedBlock
IfStatement
WhileLoop
SingleStatement
VarDeclarationStatement
Assignment
ArrayAssignment
ReturnStatement

MethodCalling
AttributeAccessing
ArrayIndexing
PostFix
Unary
BinaryOp
TernaryOp
NewArray
NewObject
Integer
Boolean
STRING
Identifier
This
 */
public class OllirGenerator implements JmmOptimization {

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public  OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        StringBuilder ollirCode = new StringBuilder(); // Convert node ...

        iterateOverCode(semanticsResult.getRootNode(), ollirCode);
        // More reports from this stage
        List<Report> reports = new ArrayList<>();
        Map<String, String> config = new HashMap<>();
        JmmParserResult result = new JmmParserResult(semanticsResult.getRootNode(), reports, config);
        return new OllirResult(semanticsResult, ollirCode.toString(), reports);
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }


    boolean hasReturn;
    Collection<String> attributes;
    public String iterateOverCode(JmmNode rootNode, StringBuilder ollirCode) {


        if (rootNode.getKind().equals("ClassDeclaration")){
            ollirCode.append(rootNode.get("className"));
            ollirCode.append(" { \n");
        }

        else if (rootNode.getKind().equals("ImportDeclaration")){
            ollirCode.append("import ");
            ollirCode.append(rootNode.get("ID"));
            ollirCode.append(";\n");
            ollirCode = dealMethodDeclaration(rootNode, ollirCode);
        }

        else if (rootNode.getKind().equals("MethodDeclaration")){
            ollirCode = dealMethodDeclaration(rootNode, ollirCode);

        }
        else if(rootNode.getKind().equals("Operation")){
            dealWithOperation(rootNode, ollirCode);
        }
        for (JmmNode childrenNode: rootNode.getChildren()) {

            ollirCode.append(iterateOverCode(childrenNode, new StringBuilder()));

        }
        if (rootNode.getKind().equals("ClassDeclaration")){
            ollirCode.append(" }");

        }
        if (rootNode.getKind().equals("MethodDeclaration")){
            ollirCode = finishMethodDeclaration(rootNode, ollirCode);
        }

        return ollirCode.toString();


    }

    private StringBuilder dealWithOperation(JmmNode jmmNode, StringBuilder ollirCode) {
        if (jmmNode.getKind().equals("OPERATION"))
            ollirCode.append(".i32 ");
        return ollirCode;
    }

    private StringBuilder finishMethodDeclaration(JmmNode rootNode, StringBuilder ollirCode) {
        String returnString = hasReturn ? "" : "\tret.V;\n\t";
        ollirCode.append("\t").append(returnString).append("}\n");
        hasReturn = false;
        return ollirCode;
    }

    private StringBuilder dealMethodDeclaration(JmmNode rootNode, StringBuilder ollirCode) {
        ollirCode.append("\t.method public ").append(rootNode.get("name")).append("(");
        return ollirCode;
    }


}
