package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.LinkedList;
import java.util.List;

public class MethodSymbolTable {
    private Type returnType;
    private List<Symbol> parameters = new LinkedList<>();
    private  ClassSymbolTable parentClass;
    private ScopeSymbolTable methodScope;

    public void setParentClass(ClassSymbolTable parent){
        this.parentClass = parent;
    }

    public String getStringRepresentation() {
        return "Lara my bomboca";
    }

    public void setMethodScope(ScopeSymbolTable methodScope) {
        this.methodScope = methodScope;
    }
}
