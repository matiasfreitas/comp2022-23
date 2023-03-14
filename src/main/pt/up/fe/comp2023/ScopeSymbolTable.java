package pt.up.fe.comp2023;


import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.LinkedList;
import java.util.List;
// Probably should be an abstract class or provide an interface that gets the current scope or returns the parent scope
public class ScopeSymbolTable {
    private List<Symbol> symbols = new LinkedList<>();
    private ScopeSymbolTable parentScope;
    private List<ScopeSymbolTable> subScopes = new LinkedList<>();

    public void addSubScope(ScopeSymbolTable subScope){
        this.subScopes.add(subScope);
    }
    public void addSymbol(Symbol symbol){
        this.symbols.add(symbol);
    }
    public void setParentScope(ScopeSymbolTable parentScope){
        this.parentScope = parentScope;
    }
}
