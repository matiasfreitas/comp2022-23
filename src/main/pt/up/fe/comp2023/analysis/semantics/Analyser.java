package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;

public abstract class Analyser {
    protected JmmSymbolTable symbolTable;
    protected JmmNode root;

    public Analyser(JmmNode root, JmmSymbolTable symbolTable){
        this.root = root;
        this.symbolTable = symbolTable;
    }

    public  abstract List<Report> analyse();


}
