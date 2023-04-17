package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.*;

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

    public String iterateOverCode(JmmNode rootNode, StringBuilder ollirCode) {
        if(rootNode.getChildren().size() == 0 ){
            if (rootNode.getKind() == "MethodDeclaration"){
                ollirCode = dealMethodDeclaration(rootNode, ollirCode);
            }
            if (rootNode.getKind() == "Operation"){
                dealWithOperation(rootNode, ollirCode);
            }
        }
        else{
            if (rootNode.getKind() == "MethodDeclaration"){
                ollirCode = dealMethodDeclaration(rootNode, ollirCode);

            }
            if (rootNode.getKind() == "Operation"){
                dealWithOperation(rootNode, ollirCode);
            }
            for (JmmNode childrenNode: rootNode.getChildren()) {
                ollirCode = new StringBuilder(iterateOverCode(childrenNode, ollirCode));

            }

            if (rootNode.getKind() == "MethodDeclaration"){
                ollirCode = finishMethodDeclaration(rootNode, ollirCode);
            }
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
