package pt.up.fe.comp2023.analysis;


import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.semantics.SemanticContextAnalyser;
import pt.up.fe.comp2023.analysis.semantics.UsageContext;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.generators.symboltable.JmmSymbolTableGen;

import java.util.LinkedList;
import java.util.List;

public class Analyser implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmSymbolTableGen symbolTableGen =  new JmmSymbolTableGen();
        List<Report> symbolTableReports = new LinkedList<>();
        symbolTableGen.visit(jmmParserResult.getRootNode(),symbolTableReports);
        JmmSymbolTable symbolTable = symbolTableGen.getJmmSymbolTable();
        System.out.println(symbolTable.print());

        SemanticContextAnalyser semanticAnalyser = new SemanticContextAnalyser(jmmParserResult.getRootNode(),symbolTable,new UsageContext());
        System.out.println("Performing Analysis");
        List<Report> reports = semanticAnalyser.analyse();
        System.out.println("Endend Analysis");
        reports.addAll(symbolTableReports);
        return new JmmSemanticsResult(jmmParserResult,symbolTable,reports);
    }
}
