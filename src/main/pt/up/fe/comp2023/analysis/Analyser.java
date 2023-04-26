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
        JmmSymbolTableGen symbolTableGen =  new JmmSymbolTableGen(jmmParserResult.getRootNode());
        List<Report> symbolTableReports = symbolTableGen.analyse();
        JmmSymbolTable symbolTable = symbolTableGen.getJmmSymbolTable();

        // TODO: Add warnings
        // TODO:      - Shadowing variables: shadowing class variables?
        // TODO:      - Unused parameters
        // TODO:      - Unused variables
        // TODO:      - Unused imports
        // TODO:      - always true conditions
        // TODO:      - missing return statement when not returning void
        SemanticContextAnalyser semanticAnalyser = new SemanticContextAnalyser(jmmParserResult.getRootNode(),symbolTable,new UsageContext());
        List<Report> reports = semanticAnalyser.analyse();
        reports.addAll(symbolTableReports);
        return new JmmSemanticsResult(jmmParserResult,symbolTable,reports);
    }
}
