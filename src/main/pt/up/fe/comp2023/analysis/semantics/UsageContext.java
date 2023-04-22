package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

public class UsageContext {

    private JmmSymbolTable symbolTable;

    private String className;
    private String methodSiganture;

    public UsageContext(JmmSymbolTable symbolTable){
        this.symbolTable = symbolTable;
    }

    public JmmSymbolTable getSymbolTable() {
        return symbolTable;
    }
}
