package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.JavammParser;

import java.util.*;
/*
All:
SingleStatement
VarDeclarationStatement
ReturnStatement
MethodCalling
BinaryOp
NewObject
This
 */
/*
IN PROGRESS
Assignment
 */
/*
MADE
classDeclaration
methodDeclaration
importDeclaration
arrayType
simpleType
Integer
Boolean
STRING
varTypeSpecification
varDeclaration
classVarDeclaration
Identifier
methodArguments
 */
public class OllirGenerator implements JmmOptimization {

    List<Report> reports = new ArrayList<>();

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
        reports.addAll(semanticsResult.getReports());
        Map<String, String> config = new HashMap<>();

        JmmParserResult result = new JmmParserResult(semanticsResult.getRootNode(), reports, config);

        return new OllirResult(semanticsResult, ollirCode.toString(), semanticsResult.getReports());
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }


    int nested = 0;
    boolean hasReturn;
    Collection<String> attributes;
    public String iterateOverCode(JmmNode rootNode, StringBuilder ollirCode) {

        if (rootNode.getKind().equals("ClassDeclaration")){
            nested ++;
            ollirCode.append(rootNode.get("className"));
            ollirCode.append(" { \n");

        }
        else if (rootNode.getKind().equals("ImportDeclaration")){
            ollirCode.append("import ");
            ollirCode.append(rootNode.get("ID"));
            ollirCode.append(";\n");
        }

        //Attributes
        else if (rootNode.getKind().equals("VarTypeSpecification") && rootNode.getJmmParent().getKind().equals("ClassDeclaration")){
            ollirCode.append(newLine());
            ollirCode.append(".field ");
            ollirCode = dealWithVar(rootNode, ollirCode);
        }
        //local variables
        else if (rootNode.getKind().equals("VarTypeSpecification")){
            ollirCode.append(newLine());
            ollirCode = dealWithVar(rootNode, ollirCode);
        }

        else if (rootNode.getKind().equals("MethodDeclaration")){
            ollirCode = dealMethodDeclaration(rootNode, ollirCode);
            return ollirCode.toString();
        }
        else if(rootNode.getKind().equals("Operation")){
            ollirCode.append(newLine());
            dealWithOperation(rootNode, ollirCode);
        }
        else if(rootNode.getKind().equals("MethodCalling")){
            ollirCode.append(newLine());
            ollirCode.append(rootNode.getChildren().get(0).get("value"));
            ollirCode.append(".");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("(");
            for (int i = 1; i < rootNode.getChildren().size()-1; i++) {
                ollirCode.append(rootNode.getChildren().get(i).get("value"));
                ollirCode.append(", ");
            }
            int lastValue = rootNode.getChildren().size()-1;
            ollirCode.append(rootNode.getChildren().get(lastValue).get("value"));
            ollirCode.append(");\n");
        }
        else if (rootNode.getKind().equals("Assignment")){
            ollirCode.append(newLine());
            ollirCode.append(rootNode.get("varName"));
            String kind = rootNode.getChildren().get(0).getKind();
            if(kind.equals("Integer")) kind = "int32";
            else if(kind.equals("Boolean")){
                kind = "bool";
            } else if (kind.equals("Identifier")) {
                System.out.println("oi");

            }
            ollirCode.append(".");
            ollirCode.append(kind);
            ollirCode.append(" :=.");
            ollirCode.append(kind);
            for (JmmNode children: rootNode.getChildren()) {
                ollirCode.append(iterateOverCode(children, new StringBuilder()));
            }
            ollirCode.append(";\n");
            return ollirCode.toString();
        }



        for (JmmNode childrenNode: rootNode.getChildren()) {
            System.out.println(childrenNode.getKind());
            ollirCode.append(iterateOverCode(childrenNode, new StringBuilder()));

        }
        if (rootNode.getKind().equals("ClassDeclaration")){
            nested --;
            ollirCode.append(newLine());
            ollirCode.append(" }\n");
        }

        else if (rootNode.getKind().equals("VarDeclaration")){
            ollirCode.append(";\n");
        }


        System.out.println(ollirCode.toString());
        return ollirCode.toString();


    }

    private String newLine(){
        StringBuilder tab = new StringBuilder();
        for (int i = 0; i < nested; i++) {
            tab.append("\t");
        }
        return tab.toString();
    }

    private StringBuilder dealWithOperation(JmmNode jmmNode, StringBuilder ollirCode) {
        if (jmmNode.getKind().equals("OPERATION"))
            ollirCode.append(".i32 ");
        return ollirCode;
    }

    private StringBuilder dealWithVar(JmmNode rootNode, StringBuilder ollirCode){
        ollirCode.append(rootNode.get("varName")).append(".");
        if(rootNode.getChildren().get(0).getChildren().get(0).getKind().equals("ArrayType")){
            ollirCode.append("array.");
            ollirCode.append(rootNode.getChildren().get(0).getChildren().get(0).getChildren().get(0).get("typeName"));
        }
        else{
            String type = rootNode.getChildren().get(0).getChildren().get(0).get("typeName");
            if (type.equals("int")) ollirCode.append("i32");
            else if (type.equals("boolean")) ollirCode.append("bool");
            else ollirCode.append(type);
        }
        return ollirCode;
    }
    private StringBuilder dealWithArguments(JmmNode rootNode, StringBuilder ollirCode){
        for (JmmNode childrenNode: rootNode.getChildren()) {
            ollirCode = dealWithVar(childrenNode, ollirCode);
            ollirCode.append(" ");
        }
        ollirCode.deleteCharAt(ollirCode.length() - 1);
        return ollirCode;
    }
    private StringBuilder dealMethodDeclaration(JmmNode rootNode, StringBuilder ollirCode) {
        ollirCode.append(newLine());
        ollirCode.append(".method ").append(rootNode.get("visibility")).append(" ");
        nested ++;
        if (rootNode.hasAttribute("isStatic")) ollirCode.append(rootNode.get("isStatic")).append(" ");
        ollirCode.append(rootNode.get("methodName")).append("(");
        for (JmmNode childrenNode: rootNode.getChildren()) {
            if (childrenNode.getKind().equals("MethodArguments")){
                ollirCode = dealWithArguments(childrenNode, ollirCode);
            }
            else if(childrenNode.getKind().equals("MethodBody")){
                ollirCode.append("){\n");
                ollirCode = new StringBuilder(iterateOverCode(childrenNode, ollirCode));

            }
        }
        nested--;
        ollirCode.append(newLine());
        ollirCode.append("}\n");
        return ollirCode;
    }


}
