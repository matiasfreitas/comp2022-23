package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
import java.util.Map;

public class ClassSymbolTable {
    private List<Symbol> classFields;
    private Map<String,MethodSymbolTable> methods;
    private Boolean isStatic;
    private String parentClass;

    public void setIsStatic(boolean b) {
        this.isStatic = true;
    }

    public void setParentClass(String extendsName) {
        this.parentClass = extendsName;
    }

    public void addMethod(MethodSymbolTable methodSymbolTable) {
        this.methods.put(methodSymbolTable.getStringRepresentation(),methodSymbolTable);
    }
}
