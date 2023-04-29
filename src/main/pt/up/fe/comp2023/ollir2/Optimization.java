package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.LinkedList;
import java.util.List;

public class Optimization implements JmmOptimization {
    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        // TODO: Need to understand the pros and cons of var in java and auto in c++
        var optimizedSemanticResult = optimize(jmmSemanticsResult);
        var ollirGenerator = new OllirGenerator();
        var rootNode =optimizedSemanticResult.getRootNode();
        var ollirReports = new LinkedList<Report>();
        var ollirCode  = ollirGenerator.visit(rootNode,ollirReports);
        return new OllirResult(optimizedSemanticResult,ollirCode,ollirReports);
    }
}
