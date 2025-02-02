package pt.up.fe.comp2023.analysis.symboltable;


import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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

    public Optional<Symbol> getSymbol(String name) {
        for (Symbol s :symbols) {
            if (s.getName().equals(name)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
    public void setParentScope(ScopeSymbolTable parentScope){
        this.parentScope = parentScope;
    }

    public String tableToString(String identation) {
        StringBuilder symbols = new StringBuilder();
        for(Symbol field : this.symbols){
            symbols.append(identation).append(field.toString()).append("\n");
        }
        StringBuilder subScopes= new StringBuilder();
        for(ScopeSymbolTable subScope : this.subScopes){
            subScopes.append(subScope.tableToString(identation + "  "));

        }
        return "" + symbols + subScopes;

    }

    public List<Symbol> flatten() {
        List<Symbol> result = new LinkedList<>(symbols);
        for(ScopeSymbolTable sub :subScopes) {
            List<Symbol> subFlat = sub.flatten();
            result.addAll(subFlat);
        }
        return result;
    }
}
