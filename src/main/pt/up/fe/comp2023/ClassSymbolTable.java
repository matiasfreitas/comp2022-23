package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
import java.util.Map;

public class ClassSymbolTable {
    private List<Symbol> classFields;
    private Map<String,MethodSymbolTable> methods;
}
