package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.List;

public class MethodSymbolTable {
    private Type returnType;
    private List<Symbol> parameters;
    private  ClassSymbolTable myClass;
    private ScopeSymbolTable methodScope;
}
