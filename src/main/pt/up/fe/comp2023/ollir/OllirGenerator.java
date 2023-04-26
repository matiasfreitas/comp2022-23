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
        // More reports from this stage
        reports.addAll(semanticsResult.getReports());
        Map<String, String> config = new HashMap<>();

        JmmParserResult result = new JmmParserResult(semanticsResult.getRootNode(), reports, config);
        System.out.println(ollirCode);
        return new OllirResult(semanticsResult, ollirCode.toString(), semanticsResult.getReports());


    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        return JmmOptimization.super.optimize(ollirResult);
    }


    int nested = 0;
    HashMap<String, String> attributes = new HashMap<>();
    boolean dontHasConstructor = true;
    boolean hasReturn = false;

    int tempCount = 0;

    public String iterateOverCodeScope(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables, String returnType) {
        //Classes
        if (rootNode.getKind().equals("ClassDeclaration")) {
            dealWithClassDeclaration(rootNode, ollirCode);
        }

        //Import
        else if (rootNode.getKind().equals("ImportDeclaration")) {
            dealWithImportDeclaration(rootNode, ollirCode);
        }

        //Attributes
        else if (rootNode.getKind().equals("ClassVarDeclaration")) {
            return dealWithClassVarDeclaration(rootNode, ollirCode, scopeVariables);
        }

        //local variables
        else if (rootNode.getKind().equals("VarDeclaration")) {
            ollirCode = dealWithVarDeclatarion(rootNode, ollirCode, scopeVariables);
        }

        //Class Methods
        //Improve Constructor part in the next phase
        else if (rootNode.getKind().equals("MethodDeclaration")) {
            if(dontHasConstructor){
                dontHasConstructor = false;
                ollirCode = createConstructors(ollirCode, rootNode);
            }

            ollirCode = dealMethodDeclaration(rootNode, ollirCode);

            return ollirCode.toString();
        }


        else if (rootNode.getKind().equals("BinaryOp")) {
            ollirCode = dealWithBinaryOp(rootNode, ollirCode, scopeVariables, "", "");
        }

        //Methods Calls
        else if (rootNode.getKind().equals("MethodCalling")) {
            ollirCode = dealWithMethodCalling(rootNode, ollirCode, scopeVariables);
        }

        //Assignments for variables
        else if (rootNode.getKind().equals("Assignment")) {
            ollirCode = dealWithAssignments(rootNode, ollirCode, scopeVariables);
            return ollirCode.toString();
        }

        //Return Statement
        else if(rootNode.getKind().equals("ReturnStatement")){
            return dealWithReturnStatement(rootNode, ollirCode, scopeVariables, returnType);
        }

        //Create code for children
        for (JmmNode childrenNode : rootNode.getChildren()) {
            ollirCode.append(iterateOverCodeScope(childrenNode, new StringBuilder(), scopeVariables, returnType));
        }

        //Finish Class Declaration
        if (rootNode.getKind().equals("ClassDeclaration")) {
            nested--;
            ollirCode.append(newLine());
            ollirCode.append(" }\n");
        }

        //Finish Return Statement
        else if (rootNode.getKind().equals("ReturnStatement")) {
            ollirCode.append(";\n");

        }


        return ollirCode.toString();
    }

    private String dealWithClassVarDeclaration(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {

        ollirCode.append(newLine());
        ollirCode.append(".field ");
        JmmNode children = rootNode.getChildren().get(0).getChildren().get(0);
        attributes.put(children.get("varName"), dealWithType(children.getChildren().get(0), scopeVariables));
        ollirCode = dealWithVar(children, ollirCode, attributes);

        return ollirCode.append(";\n").toString();
    }

    private void dealWithImportDeclaration(JmmNode rootNode, StringBuilder ollirCode) {
        ollirCode.append("import ");
        ollirCode.append(String.join( ".", rootNode.get("moduleName")
                .replace("[", "").replace("]", "")
                .replace(", ", ".")));

        ollirCode.append(";\n");
    }

    private StringBuilder dealWithVarDeclatarion(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {
        JmmNode children = rootNode.getChildren().get(0);
        if(children.hasAttribute("varName")){
            String name = children.get("varName");
            String kind = dealWithType(children.getChildren().get(0), scopeVariables);
            scopeVariables.put(name, kind);
        }
        else{
            String name = children.get("type");
            String kind = dealWithType(children, scopeVariables);
            scopeVariables.put(name, kind);
        }
        return ollirCode;
    }

    private String dealWithReturnStatement(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables, String returnType) {
        hasReturn = true;
        if (!rootNode.getChildren().get(0).hasAttribute("varName") &&
                !rootNode.getChildren().get(0).hasAttribute("value")){
            ollirCode.append(newLine());
            ollirCode.append("returnVariable.");
            ollirCode.append(returnType);
            ollirCode.append(":=.");
            ollirCode.append(returnType + " ");
            for (JmmNode children:  rootNode.getChildren()) {
                ollirCode.append(iterateOverCodeScope(children, new StringBuilder(), scopeVariables, returnType));
            }
            ollirCode.append(";");
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

    private void dealWithClassDeclaration(JmmNode rootNode, StringBuilder ollirCode) {

        nested++;
        ollirCode.append(rootNode.get("className"));

        if (rootNode.hasAttribute("extendsName")) {
            ollirCode.append(" extends ");
            ollirCode.append(rootNode.get("extendsName"));
        }

        ollirCode.append(" { \n\n");
        ollirCode.append(newLine());
    }

    private StringBuilder dealWithMethodCalling(JmmNode rootNode, StringBuilder ollirCode, HashMap<String,
            String> scopeVariables) {

        //Variable verification
        String variable = rootNode.getChildren().get(0).get("value");
        boolean isScopedVariable = scopeVariables.containsKey(variable);
        boolean isAttribute = attributes.containsKey(variable);
        boolean isThis = variable.equals("this");

        //Package verification
        String thisPackage = rootNode.getChildren().get(0).get("value");
        List<String> packages = semanticsResult.getSymbolTable().getImports();
        boolean isPackage = packages.stream().anyMatch(s -> s.equals(thisPackage));
        ollirCode.append(newLine());


        if(isScopedVariable || isAttribute || isThis) {

            ollirCode.append("invokevirtual(");

            ollirCode.append(variable);
            ollirCode.append(".");

            if(isScopedVariable) ollirCode.append(scopeVariables.get(variable));
            else if(isAttribute) ollirCode.append(attributes.get(variable));
            else if(isThis) ollirCode.deleteCharAt(ollirCode.length()-1);

            ollirCode.append(", \"");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("\"");

            for (int i = 1; i < rootNode.getChildren().size(); i++) {
                ollirCode.append(", ");
                ollirCode = dealWithVar(rootNode.getChildren().get(i), ollirCode, scopeVariables);
            }
            ollirCode.append(").");
            String assignmentVariable = "";
            if (rootNode.getJmmParent().hasAttribute("varName")) assignmentVariable = rootNode.getJmmParent().get("varName");

            boolean isAssignmentVariable =  scopeVariables.containsKey(assignmentVariable);
            boolean isAttributeVariable =  attributes.containsKey(assignmentVariable);

            if(rootNode.getJmmParent().getKind().equals("Assignment") && isAssignmentVariable)
                ollirCode.append(scopeVariables.get(assignmentVariable));
            else if(rootNode.getJmmParent().getKind().equals("Assignment") && isAttributeVariable)
                ollirCode.append(attributes.get(assignmentVariable));
            else ollirCode.append("V");
            ollirCode.append(";\n");
        }
        else if(isPackage){

            ollirCode.append("invokestatic(");
            ollirCode.append(rootNode.getChildren().get(0).get("value"));
            ollirCode.append(", \"");
            ollirCode.append(rootNode.get("methodName"));
            ollirCode.append("\"");

            for (int i = 1; i < rootNode.getChildren().size(); i++) {
                ollirCode.append(", ");


                ollirCode = dealWithVar(rootNode.getChildren().get(i), ollirCode, scopeVariables);

            }

            ollirCode.append(").V;\n");
        }
        return ollirCode;
    }

    private StringBuilder createConstructors(StringBuilder ollirCode, JmmNode rootNode) {

        ollirCode.append(newLine());
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
        String typeKind = rootNode.getKind();

        if (typeKind.equals("Integer")) return ollirCode.append("i32").toString();
        if (typeKind.equals("Int")) return ollirCode.append("i32").toString();
        else if (typeKind.equals("boolean")) return  ollirCode.append("bool").toString();
        else if (typeKind.equals("STRING")) return ollirCode.append("String").toString();
        else if (typeKind.equals("void")) return ollirCode.append("V").toString();
        else if (typeKind.equals("Identifier")) typeKind = rootNode.get("type");

        JmmNode type = rootNode;
        if (rootNode.getChildren().size() >0) type = rootNode.getChildren().get(0);

        if (type.getKind().equals("ArrayType")) {
            ollirCode.append("array.");
            type = type.getChildren().get(0);
        }

        if(type.hasAttribute("typeName")) typeKind = type.get("typeName");
        else if (type.getKind().equals("Identifier")) typeKind = type.get("type");
        else if (type.getKind().equals("MethodCalling")) typeKind = scopeVariables.get(rootNode.get("varName"));
        else if (rootNode.getKind().equals("ReturnStatement")) typeKind = type.getKind();
        else if (rootNode.getKind().equals("Assignment")) typeKind = rootNode.getJmmChild(0).get("type");
        else if (scopeVariables.containsKey(type.get("varName"))) typeKind = scopeVariables.get(type.get("varName"));
        else if (attributes.containsKey(type.get("varName"))) typeKind = attributes.get(type.get("varName"));



        if (typeKind.equals("int")) ollirCode.append("i32");
        else if (typeKind.equals("Int")) ollirCode.append("i32");
        else if (typeKind.equals("Integer")) ollirCode.append("i32");
        else if (typeKind.equals("boolean")) ollirCode.append("bool");
        else if (typeKind.equals("STRING")) ollirCode.append("String");
        else if (typeKind.equals("void")) ollirCode.append("V");

        else
            ollirCode.append(typeKind);


        return ollirCode.toString();
    }

    private StringBuilder dealWithVar(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables) {
        if(rootNode.hasAttribute("varName")){
            String name = rootNode.get("varName");
            ollirCode.append(name).append(".");

            String kind = dealWithType(rootNode.getChildren().get(0), scopeVariables);
            ollirCode.append(kind);

            scopeVariables.put(name, kind);
        }
        else if(rootNode.hasAttribute("value")){
            String name = rootNode.get("type");
            ollirCode.append(rootNode.get("value")).append(".");

            String kind = dealWithType(rootNode, scopeVariables);
            ollirCode.append(kind);

            scopeVariables.put(name, kind);
        }
        else if(rootNode.hasAttribute("methodName")){
            String name = rootNode.get("type");
            ollirCode.append(rootNode.get("methodName")).append(".");

            String kind = dealWithType(rootNode, scopeVariables);
            ollirCode.append(kind);

            scopeVariables.put(name, kind);
        }
        return ollirCode;
    }

    private StringBuilder dealWithArguments(JmmNode rootNode, StringBuilder ollirCode,
                                            HashMap<String, String> scopeVariables) {


        int i;
        for (i = 0; i < rootNode.getNumChildren() - 1; i++ ) {

            ollirCode = dealWithVar(rootNode.getJmmChild(i), ollirCode, scopeVariables);
            ollirCode.append(", ");
        }

        if (i < rootNode.getNumChildren())
            ollirCode = dealWithVar(rootNode.getJmmChild(i), ollirCode, scopeVariables);

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
                ollirCode.append(iterateOverCodeScope(children, new StringBuilder(), variables, type));
            }
        }

        if(! hasReturn){
            ollirCode.append(newLine());
            ollirCode.append("ret.V;\n");
        }

        nested--;
        ollirCode.append(newLine());
        ollirCode.append("}\n\n");
        hasReturn = false;
        return ollirCode;
    }

    public StringBuilder dealWithAssignments(JmmNode rootNode, StringBuilder ollirCode, HashMap<String, String> scopeVariables){

        ollirCode.append(newLine());

        StringBuilder newExpression= new StringBuilder();

        String originalVar = rootNode.get("varName");
        //Deal with Atributtes:
        if(attributes.containsKey(rootNode.get("varName"))){
            newExpression.append("pulField(this, ");
            newExpression.append(originalVar);
            ollirCode.append(".");
            newExpression.append(attributes.get(rootNode.get("varName")));
            newExpression.append(",");

            for (JmmNode children : rootNode.getChildren()) {
                if (children.getKind().equals("Integer")) {
                    newExpression.append(children.get("value"));
                    newExpression.append(".i32 ");
                }
                else if (children.getKind().equals("boolean")) {
                    newExpression.append(children.get("value"));
                    newExpression.append(".bool ");
                }
                else if (children.equals("Identifier")) {
                    newExpression.append(children.getKind());
                    newExpression.append(children.get("varName"));
                }
                else if(children.getKind().equals("MethodCalling")){
                    ollirCode   = dealWithMethodCalling(rootNode.getJmmChild(0), ollirCode , scopeVariables);
                }

            }
        }
        else{

            String type = dealWithType(rootNode, scopeVariables);
            JmmNode children = rootNode.getChildren().get(0);
            if(children.getKind().equals("NewObject")){

                newExpression.append(rootNode.get("varName") + "." + type + " :=." + type + " new(" + type + ")." + type + ";\n");
                newExpression.append(newLine());
                newExpression.append("invokespecial(");
                newExpression.append(rootNode.get("varName"));
                newExpression.append(".");
                newExpression.append(type);
                newExpression.append(",\"<init>\").V");
            }
            else {
                newExpression.append(rootNode.get("varName"));
                newExpression.append(".");
                newExpression.append(type);
                newExpression.append(" :=.");
                newExpression.append(type);
                newExpression.append(" ");

                //TODO Martim vê isto
                if (children.getKind().equals("Int")) {
                    newExpression.append(children.get("value"));
                    newExpression.append(".i32 ");
                } else if (children.getKind().equals("Boolean")) {
                    newExpression.append(children.get("value"));
                    newExpression.append(".bool ");
                } else if (children.getKind().equals("Identifier")) {
                    newExpression.append(children.get("value"));
                    newExpression.append(".");
                    newExpression.append(scopeVariables.get(children.get("value")));
                }
                else if(children.getKind().equals("MethodCalling")){
                    ollirCode.append(newExpression);
                    ollirCode  = dealWithMethodCalling(rootNode.getJmmChild(0), ollirCode , scopeVariables);
                    return newExpression;
                }
                else if(children.getKind().equals("BinaryOp")){
                    ollirCode = dealWithBinaryOp(children, ollirCode, scopeVariables, "temp" + String.valueOf(tempCount), originalVar);
                    tempCount++;
                    ollirCode.append(";\n");
                    return ollirCode;
                }

            }
        }
        ollirCode.append(newExpression);

        ollirCode.append(";\n");
        return ollirCode;
    }

    private StringBuilder dealWithBinaryOp(JmmNode rootNode, StringBuilder ollirCode, HashMap<String,
            String> scopeVariables, String tempVar, String originalVar) {
        StringBuilder expression = new StringBuilder();

        String firstTerm;
        String secondTerm;
        String aux;
        String op = rootNode.get("op");

        String type;
        if (rootNode.get("type").equals("boolean")) {
            type = (".bool");
        }
        else if (rootNode.get("type").equals("int")) {
            type = (".i32");
        }
        else
            type = ".V";
        expression.append(newLine());
        if (rootNode.getJmmChild(0).getKind().equals("MethodCalling")){
            firstTerm = rootNode.getJmmParent().get("varName");
            scopeVariables.put(firstTerm, type);
            expression.append(tempVar).append(type).append(" :=").append(type).append(" ");
            expression = (dealWithMethodCalling(rootNode.getJmmChild(0), expression, scopeVariables));
            tempCount++;
            expression.append(newLine());
            expression = expression.append(originalVar).append(type).append(" :=").append(type);
            firstTerm = tempVar;

        }
        else{
            firstTerm = rootNode.getJmmChild(0).get("value");
        }

        if (rootNode.getJmmChild(1).getKind().equals("MethodCalling")){
            secondTerm = rootNode.getJmmParent().get("varName");
            scopeVariables.put(secondTerm, type);
            expression.append(tempVar).append(type).append(" :=").append(type).append(" ");
            expression = (dealWithMethodCalling(rootNode.getJmmChild(1), expression, scopeVariables));
            tempCount++;
            expression.append(newLine());
            expression = expression.append(originalVar).append(type).append(" :=").append(type);
            secondTerm = tempVar;
        }
        else{
            secondTerm = rootNode.getJmmChild(1).get("value");
        }


        expression.append(" ").append( firstTerm + type + " ");
        expression.append(rootNode.get("op"));
        expression.append(type + " " + secondTerm + type);

        ollirCode.append(expression);
        return ollirCode;

    }
    private StringBuilder dealWithOperation(JmmNode rootNode, StringBuilder ollirCode) {
        StringBuilder expression = new StringBuilder();

        String firstTerm;
        String secondTerm;
        String aux;
        String op = rootNode.get("op");

        if (rootNode.getJmmChild(0).getKind().equals("Integer") || rootNode.getJmmChild(0).getKind().equals("Identifier")) {
            firstTerm = rootNode.getJmmChild(0).get("value");
            expression.append(":=.i32 ");
            expression.append(firstTerm);
            expression.append(".i32");
        } else {
            aux = "";
            for (JmmNode children : rootNode.getJmmChild(0).getChildren()) {
                aux.concat(dealWithOperation(children, ollirCode).toString());
            }
            expression.append(aux);
        }
        if (rootNode.getJmmChild(1).getKind().equals("Integer") || rootNode.getJmmChild(1).getKind().equals("Identifier")) {
            secondTerm = rootNode.getJmmChild(1).get("value");
            expression.append(op);
            expression.append(".i32 ");
            expression.append(secondTerm);
            expression.append(".i32");
        } else {
            aux = "";
            for (JmmNode child : rootNode.getJmmChild(1).getChildren()) {
                aux.concat(dealWithOperation(child, ollirCode).toString());
            }
            expression.append(aux);
        }

        expression.append(";\n");

        return expression;
    }
}
