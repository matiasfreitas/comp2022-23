package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.List;
// Probably should be an abstract class or provide an interface that gets the current scope or returns the parent scope
public class ScopeSymbolTable {
    private List<Symbol> symbols;
    private ScopeSymbolTable parentScope;
    private List<ScopeSymbolTable> subScopes;
}
