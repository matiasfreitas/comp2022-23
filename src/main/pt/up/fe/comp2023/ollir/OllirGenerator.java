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
Assignment
 */
public class OllirGenerator implements JmmOptimization {

    List<Report> reports = new ArrayList<>();

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        StringBuilder ollirCode = new StringBuilder(); // Convert node ...

        iterateOverCode(semanticsResult.getRootNode(), ollirCode, new HashMap<>());
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
    HashMap<String, String> attributes;

    public String iterateOverCode(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {

        if (rootNode.getKind().equals("ClassDeclaration")) {
            nested++;
            ollirCode.append(rootNode.get("className"));
            ollirCode.append(" { \n\n");
            ollirCode.append(newLine());

            //Constructor
            ollirCode.append(".construct ");
            ollirCode.append(rootNode.get("className"));
            ollirCode.append("().V {\n");
            nested++;
            ollirCode.append(newLine());
            ollirCode.append("invokespecial(this, \"<init>\").V;\n");
            nested--;
            ollirCode.append(newLine());
            ollirCode.append("}\n\n");

        } else if (rootNode.getKind().equals("ImportDeclaration")) {
            ollirCode.append("import ");
            ollirCode.append(rootNode.get("ID"));
            ollirCode.append(";\n");
        }

        //Attributes
        else if (rootNode.getKind().equals("VarDeclarationStatement") && rootNode.getJmmParent().getKind().equals("ClassDeclaration")) {
            ollirCode.append(newLine());
            ollirCode.append(".field ");
            JmmNode children = rootNode.getChildren().get(0).getChildren().get(0);
            ollirCode = dealWithVar(children, ollirCode, attributes);
        }

        //local variables
        else if (rootNode.getKind().equals("VarDeclarationStatement")) {
            ollirCode.append(newLine());
            JmmNode children = rootNode.getChildren().get(0).getChildren().get(0);
            ollirCode = dealWithVar(children, ollirCode, scopeVariables);
        }
        else if (rootNode.getKind().equals("MethodDeclaration")) {
            ollirCode = dealMethodDeclaration(rootNode, ollirCode);
            return ollirCode.toString();
        }
        else if (rootNode.getKind().equals("Operation")) {
            ollirCode.append(newLine());
            dealWithOperation(rootNode, ollirCode);
        }
        else if (rootNode.getKind().equals("MethodCalling")) {
            ollirCode.append(newLine());
            ollirCode.append(rootNode.getChildren().get(0).get("value"));
            ollirCode.append(".");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("(");
            for (int i = 1; i < rootNode.getChildren().size() - 1; i++) {
                ollirCode.append(rootNode.getChildren().get(i).get("value"));
                ollirCode.append(", ");
            }

            int lastValue = rootNode.getChildren().size() - 1;
            ollirCode.append(rootNode.getChildren().get(lastValue).get("value"));
            ollirCode.append(");\n");
        }
        else if (rootNode.getKind().equals("Assignment")) {
            ollirCode.append(newLine());
            ollirCode.append(rootNode.get("varName"));

            String type = dealWithType(rootNode, scopeVariables);

            ollirCode.append(".");
            ollirCode.append(type);
            ollirCode.append(" :=.");
            ollirCode.append(type);
            ollirCode.append(" ");

            for (JmmNode children : rootNode.getChildren()) {
                if (children.getKind().equals("Integer")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".i32 ");
                }
                else if (children.getKind().equals("Boolean")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".bool ");
                }
                else if (children.equals("Identifier")) {
                    ollirCode.append(children.getKind());
                    ollirCode.append(children.get("varName"));
                }

            }

            ollirCode.append(";\n");
            return ollirCode.toString();
        }

        else if(rootNode.getKind().equals("ReturnStatement")){
            System.out.println("oi");

        }

        else if(rootNode.getKind().equals("BinaryOp")){
            System.out.println("oi");

        }


        for (JmmNode childrenNode : rootNode.getChildren()) {

            System.out.println(childrenNode.getKind());
            ollirCode.append(iterateOverCode(childrenNode, new StringBuilder(), new HashMap<>()));
        }

        if (rootNode.getKind().equals("ClassDeclaration")) {

            nested--;
            ollirCode.append(newLine());
            ollirCode.append(" }\n");
        }
        else if (rootNode.getKind().equals("VarDeclaration")) {
            ollirCode.append(";\n");
        }

        return ollirCode.toString();
    }

    private String newLine() {

        StringBuilder tab = new StringBuilder();

        for (int i = 0; i < nested; i++) {
            tab.append("\t");
        }

        return tab.toString();
    }

    private String dealWithType(JmmNode rootNode, HashMap<String, String> scopeVariables){

        StringBuilder ollirCode = new StringBuilder();

        JmmNode type = rootNode.getChildren().get(0);
        String typeKind;

        if (type.getKind().equals("ArrayType")) {
            ollirCode.append("array.");
            type = type.getChildren().get(0);
        }

        if (rootNode.getKind().equals("Assignment")) typeKind = type.getKind();
        else typeKind = type.get("typeName");

        if (typeKind.equals("int")) ollirCode.append("i32");
        else if (typeKind.equals("Integer")) ollirCode.append("i32");
        else if (typeKind.equals("Boolean")) ollirCode.append("bool");
        else if (typeKind.equals("STRING")) ollirCode.append("String");
        else if (typeKind.equals("void")) ollirCode.append("V");
        else if (typeKind.equals("Identifier")) {
            scopeVariables.get(rootNode.get("varName"));
        }
        else{
            ollirCode.append(typeKind);
        }

        return ollirCode.toString();
    }

    private StringBuilder dealWithOperation(JmmNode rootNode, StringBuilder ollirCode) {

        if (rootNode.getKind().equals("OPERATION")){}

        return ollirCode;
    }

    private StringBuilder dealWithVar(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {

        String name = rootNode.get("varName");
        ollirCode.append(name).append(".");

        String kind = dealWithType(rootNode.getChildren().get(0), scopeVariables);
        ollirCode.append(kind);

        scopeVariables.put(name, kind);
        return ollirCode;
    }

    private StringBuilder dealWithArguments(JmmNode rootNode, StringBuilder ollirCode) {

        for (JmmNode childrenNode : rootNode.getChildren()) {

            ollirCode = dealWithVar(childrenNode, ollirCode, new HashMap<>());
            ollirCode.append(" ");
        }

        ollirCode.deleteCharAt(ollirCode.length() - 1);
        return ollirCode;
    }

    private StringBuilder dealMethodDeclaration(JmmNode rootNode, StringBuilder ollirCode) {

        ollirCode.append(newLine());
        ollirCode.append(".method ").append(rootNode.get("visibility")).append(" ");

        nested++;

        if (rootNode.hasAttribute("isStatic")) ollirCode.append(rootNode.get("isStatic")).append(" ");


        List<JmmNode> childrens =  rootNode.getChildren();

        int index = 0;
        String type = ".V";

        if (childrens.size() > index && childrens.get(index).getKind().equals("Type")){

            type = dealWithType(childrens.get(index), new HashMap<>());

            index++;
        }


        ollirCode.append(rootNode.get("methodName")).append("(");

        if (childrens.size() > index && childrens.get(index).getKind().equals("MethodArguments")){

            ollirCode = dealWithArguments(childrens.get(index), ollirCode);


            index++;
        }



        ollirCode.append(")");
        ollirCode.append(".");
        if (type.equals(""))  ollirCode.append(".V");
        else ollirCode.append(type);
        ollirCode.append(" ");
        ollirCode.append("{\n\n");

        if (childrens.size() > index && childrens.get(index).getKind().equals("MethodBody")){

            ollirCode = new StringBuilder(iterateOverCode(childrens.get(index), ollirCode, new HashMap<>()));

            index++;
        }

        nested--;
        ollirCode.append(newLine());
        ollirCode.append("}\n\n");
        return ollirCode;
    }


}
