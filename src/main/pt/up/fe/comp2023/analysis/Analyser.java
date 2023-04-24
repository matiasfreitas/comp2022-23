package pt.up.fe.comp2023.analysis;


import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.semantics.SemanticAnalyser;
import pt.up.fe.comp2023.analysis.semantics.UsageContext;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.analysis.generators.symboltable.JmmSymbolTableGen;

import java.util.ArrayList;
import java.util.List;

public class Analyser implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        JmmSymbolTableGen symbolTableGen =  new JmmSymbolTableGen();
        symbolTableGen.visit(jmmParserResult.getRootNode());
        JmmSymbolTable symbolTable = symbolTableGen.getJmmSymbolTable();

        /*
        SemanticAnalyser semanticAnalyser = new SemanticAnalyser(jmmParserResult.getRootNode(),symbolTable,new UsageContext());
        List<Report> reports = semanticAnalyser.analyse();
         */
        return new JmmSemanticsResult(jmmParserResult,symbolTable, new ArrayList<>());
    }
}
