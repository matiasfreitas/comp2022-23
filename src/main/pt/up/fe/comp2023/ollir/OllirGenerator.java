package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

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
    JmmSemanticsResult semanticsResult;

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        return JmmOptimization.super.optimize(semanticsResult);
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {
        this.semanticsResult = semanticsResult;
        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        StringBuilder ollirCode = new StringBuilder(); // Convert node ...

        iterateOverCodeScope(semanticsResult.getRootNode(), ollirCode, new HashMap<>(), "");

        System.out.println(ollirCode.toString());
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
    HashMap<String, String> attributes = new HashMap<>();
    boolean dontHasConstructor = true;

    public String iterateOverCodeScope(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables, String returnType) {

        if (rootNode.getKind().equals("ClassDeclaration")) {
            nested++;
            ollirCode.append(rootNode.get("className"));
            if (rootNode.hasAttribute("extendsName")) {
                ollirCode.append(" extends ");
                ollirCode.append(rootNode.get("extendsName"));
            }
            ollirCode.append(" { \n\n");
            ollirCode.append(newLine());
        }

        else if (rootNode.getKind().equals("ImportDeclaration")) {
            ollirCode.append("import ");
            ollirCode.append(String.join( ".", rootNode.get("moduleName")
                    .replace("[", "").replace("]", "")
                    .replace(", ", ".")));

            ollirCode.append(";\n");
        }

        //Attributes
        else if (rootNode.getKind().equals("ClassVarDeclaration")) {

            ollirCode.append(newLine());
            ollirCode.append(".field ");
            JmmNode children = rootNode.getChildren().get(0).getChildren().get(0);
            attributes.put(children.get("varName"), dealWithType(children.getChildren().get(0), scopeVariables));
            ollirCode = dealWithVar(children, ollirCode, attributes);
            return ollirCode.append(";\n").toString();
        }

        //local variables
        else if (rootNode.getKind().equals("VarDeclaration")) {
            ollirCode.append(newLine());
            JmmNode children = rootNode.getChildren().get(0);
            ollirCode = dealWithVar(children, ollirCode, scopeVariables);
        }
        else if (rootNode.getKind().equals("MethodDeclaration")) {

            if(dontHasConstructor){
                dontHasConstructor = false;
                ollirCode = createConstructors(ollirCode, rootNode);
            }

            ollirCode = dealMethodDeclaration(rootNode, ollirCode);
            return ollirCode.toString();
        }
        else if (rootNode.getKind().equals("BinaryOp")) {
            ollirCode.append(dealWithOperation(rootNode, ollirCode).toString());
        }
        else if (rootNode.getKind().equals("MethodCalling")) {
            ollirCode.append(dealWithMethodCalling(rootNode, ollirCode).toString());
        }
        else if (rootNode.getKind().equals("Assignment")) {
            ollirCode = dealWithAssignments(rootNode, ollirCode, scopeVariables);
        }

        else if(rootNode.getKind().equals("ReturnStatement")){
            if (!rootNode.getChildren().get(0).hasAttribute("varName") &&
                    !rootNode.getChildren().get(0).hasAttribute("value")){
                ollirCode.append(newLine());
                ollirCode.append("returnVariable ");
                for (JmmNode children:  rootNode.getChildren()) {
                    ollirCode.append(iterateOverCodeScope(children, new StringBuilder(), scopeVariables, returnType));
                }
                ollirCode.append(newLine());
                ollirCode.append("ret.");
                ollirCode.append(returnType);
                ollirCode.append(" returnVariable.");
                ollirCode.append(returnType);
                ollirCode.append(";\n");
                return ollirCode.toString();
            }
            else{
                ollirCode.append(newLine());
                ollirCode.append("ret.");
                ollirCode.append(returnType);
                ollirCode.append(" ");
                if (rootNode.getChildren().get(0).hasAttribute("value")){
                    ollirCode.append(rootNode.getChildren().get(0).get("value"));
                }
                else if (rootNode.getChildren().get(0).hasAttribute("varName")){
                    ollirCode.append(rootNode.getChildren().get(0).get("varName"));
                }
                ollirCode.append(".");
                ollirCode.append(returnType);
                ollirCode.append(";\n");
                return ollirCode.toString();
            }


        }



        for (JmmNode childrenNode : rootNode.getChildren()) {


            ollirCode.append(iterateOverCodeScope(childrenNode, new StringBuilder(), scopeVariables, returnType));
        }

        if (rootNode.getKind().equals("ClassDeclaration")) {

            nested--;
            ollirCode.append(newLine());
            ollirCode.append(" }\n");
        }
        else if (rootNode.getKind().equals("VarDeclaration")) {
            ollirCode.append(";\n");
        }
        else if (rootNode.getKind().equals("ReturnStatement")) {
            ollirCode.append(";\n");
        }


        return ollirCode.toString();
    }

    private StringBuilder dealWithMethodCalling(JmmNode rootNode, StringBuilder ollirCode) {
        ollirCode.append(newLine());
        String packages = new StringBuilder().append(rootNode.getChildren().get(0).get("value")).toString();
        if(semanticsResult.getSymbolTable().getMethods().stream().anyMatch(s -> s.equals(rootNode.get("methodName")))) {
            ollirCode.append("invokespecial(");
            ollirCode.append(rootNode.getChildren().get(0).get("value"));
            ollirCode.append(", \"");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("\"");
            for (int i = 1; i < rootNode.getChildren().size(); i++) {
                ollirCode.append(", ");
                ollirCode.append(rootNode.getChildren().get(i).get("value"));
            }
            ollirCode.append(");\n");
        }
        else if(rootNode.getChildren().get(0).hasAttribute("value") &&
                semanticsResult.getSymbolTable().getImports().stream().anyMatch(s -> s.equals(packages))){
            ollirCode.append("invokestatic(");
            ollirCode.append(rootNode.getChildren().get(0).get("value"));
            ollirCode.append(".");
            ollirCode.append(rootNode.getChildren().get(1).get("value"));
            ollirCode.append(", \"");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("\"");
            for (int i = 2; i < rootNode.getChildren().size(); i++) {
                ollirCode.append(", ");
                ollirCode.append(rootNode.getChildren().get(i).get("value"));
            }
            ollirCode.append(");\n");
        }
        return ollirCode;
    }

    private StringBuilder createConstructors(StringBuilder ollirCode, JmmNode rootNode) {
        ollirCode.append(newLine());
        //Constructor
        ollirCode.append(".construct ");
        ollirCode.append(rootNode.getJmmParent().get("className"));
        ollirCode.append("().V {\n");
        nested++;
        ollirCode.append(newLine());
        ollirCode.append("invokespecial(this, \"<init>\").V;\n");
        nested--;
        ollirCode.append(newLine());
        ollirCode.append("}\n\n");

        return ollirCode;
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
        else if (rootNode.getKind().equals("ReturnStatement")) typeKind = type.getKind();
        else if(type.hasAttribute("typeName")) typeKind = type.get("typeName");
        else typeKind = scopeVariables.get(type.get("varName"));

        if (typeKind.equals("int")) ollirCode.append("i32");
        else if (typeKind.equals("Integer")) ollirCode.append("i32");
        else if (typeKind.equals("boolean")) ollirCode.append("bool");
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

    private StringBuilder dealWithVar(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {

        String name = rootNode.get("varName");
        ollirCode.append(name).append(".");

        String kind = dealWithType(rootNode.getChildren().get(0), scopeVariables);
        ollirCode.append(kind);

        scopeVariables.put(name, kind);
        return ollirCode;
    }

    private StringBuilder dealWithArguments(JmmNode rootNode, StringBuilder ollirCode,
                                            HashMap<String, String> scopeVariables) {

        for (JmmNode childrenNode : rootNode.getChildren()) {

            ollirCode = dealWithVar(childrenNode, ollirCode, scopeVariables);
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
        String type =  "";

        HashMap<String, String> variables = new HashMap<>();
        if (childrens.size() > index && childrens.get(index).getKind().equals("Type")){

            type = dealWithType(childrens.get(index), variables);

            index++;
        }

        ollirCode.append(rootNode.get("methodName")).append("(");


        if (childrens.size() > index && childrens.get(index).getKind().equals("MethodArguments")){

            ollirCode = dealWithArguments(childrens.get(index), ollirCode, variables);


            index++;
        }



        ollirCode.append(")");
        ollirCode.append(".");
        if (type.equals(""))  ollirCode.append(".V");
        else ollirCode.append(type);
        ollirCode.append(" ");
        ollirCode.append("{\n\n");

        if (childrens.size() > index && childrens.get(index).getKind().equals("MethodBody")){
            for (JmmNode children: childrens.get(index).getChildren()) {
                ollirCode = new StringBuilder(iterateOverCodeScope(children, ollirCode, variables, type));
            }

            index++;
        }


        nested--;
        ollirCode.append(newLine());
        ollirCode.append("}\n\n");
        return ollirCode;
    }

    public StringBuilder dealWithAssignments(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables){

        ollirCode.append(newLine());

        StringBuilder newExpression = new StringBuilder();
        //Deal with Atributtes:
        if(attributes.containsKey(rootNode.get("varName"))){
            ollirCode.append("pulField(this, ");
            ollirCode.append(rootNode.get("varName"));
            ollirCode.append(".");
            ollirCode.append(attributes.get(rootNode.get("varName")));
            ollirCode.append(",");

            for (JmmNode children : rootNode.getChildren()) {
                if (children.getKind().equals("Integer")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".i32 ");
                }
                else if (children.getKind().equals("boolean")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".bool ");
                }
                else if (children.equals("Identifier")) {
                    ollirCode.append(children.getKind());
                    ollirCode.append(children.get("varName"));
                }

            }
        }
        else{
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
                else if (children.getKind().equals("boolean")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".bool ");
                }
                else if (children.getKind().equals("Identifier")) {
                    ollirCode.append(children.get("value"));
                    ollirCode.append(".");
                    ollirCode.append(scopeVariables.get(children.get("value")));
                }

            }

        }


        ollirCode.append(";\n");
        return ollirCode;
    }


    private StringBuilder dealWithOperation(JmmNode rootNode, StringBuilder ollirCode) {
        StringBuilder expression = new StringBuilder();
        String op = rootNode.get("op");
        String firstTerm;
        String secondTerm;
        String aux;


        if(rootNode.getJmmChild(0).getKind().equals("Integer") || rootNode.getJmmChild(0).getKind().equals("Identifier")) {
            firstTerm = rootNode.getJmmChild(0).get("value");

            expression.append(":=.i32 ");
            expression.append(firstTerm);
            expression.append(".i32");
        } else{
            aux = "";
            for(JmmNode children: rootNode.getJmmChild(0).getChildren()){
                aux.concat(dealWithOperation(children,ollirCode).toString());
            }
            expression.append(aux);
        }
        if(rootNode.getJmmChild(1).getKind().equals("Integer") || rootNode.getJmmChild(1).getKind().equals("Identifier")){
            secondTerm = rootNode.getJmmChild(1).get("value");
            expression.append(op);
            expression.append(".i32 ");
            expression.append(secondTerm);
            expression.append(".i32");
        }
        else{
            aux = "";
            for(JmmNode child: rootNode.getJmmChild(1).getChildren()){
                aux.concat(dealWithOperation(child,ollirCode).toString());
            }
            expression.append(aux);
        }

        expression.append(";\n");

        return expression;
    }
}
