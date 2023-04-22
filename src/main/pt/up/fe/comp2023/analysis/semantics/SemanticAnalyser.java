package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyser extends  Analyser {

    public SemanticAnalyser(JmmNode root, JmmSymbolTable symbolTable){
        super(root,symbolTable);
    }

    @Override
    public List<Report> analyse(){
        List<Report> reports = new ArrayList<>();
        return  reports;

    }
}
