package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.optimization.RegisterOptimizer;

import java.util.LinkedList;

public class Optimization implements JmmOptimization {
    private int nRegisters;

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        // TODO: Need to understand the pros and cons of var in java and auto in c++
        var optimizedSemanticResult = optimize(jmmSemanticsResult);
        // Is this cast dangerous?
        System.err.println("Im being called");
        var ollirGenerator = new OllirGenerator((JmmSymbolTable) jmmSemanticsResult.getSymbolTable());
        var rootNode =optimizedSemanticResult.getRootNode();
        var ollirReports = new LinkedList<Report>();
        var ollirCode  = ollirGenerator.visit(rootNode,ollirReports);
        return new OllirResult(optimizedSemanticResult,ollirCode,ollirReports);
    }


}
