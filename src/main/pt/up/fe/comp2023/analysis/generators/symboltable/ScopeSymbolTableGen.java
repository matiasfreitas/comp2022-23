package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.semantics.Analyser;
import pt.up.fe.comp2023.analysis.symboltable.ScopeSymbolTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ScopeSymbolTableGen extends Analyser<Void> {
    ScopeSymbolTable thisScope;
    List<Symbol> classFields;
    List<Symbol> methodParameters;

    public ScopeSymbolTableGen(JmmNode root, List<Symbol> classFields, List<Symbol> methodParameters) {
        super(root);
        this.thisScope = new ScopeSymbolTable();
        this.classFields = classFields;
        this.methodParameters = methodParameters;
    }

    @Override
    protected void buildVisitor() {
        addVisit("VarTypeSpecification", this::handleVarDeclaration);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleVarDeclaration(JmmNode jmmNode, List<Report> reports) {
        SymbolGen symbolGen = new SymbolGen();
        symbolGen.visit(jmmNode);
        Symbol s = symbolGen.getSymbol();
        Optional<Symbol> maybeDefined = this.thisScope.getSymbol(s.getName());
        Optional<Symbol> maybeParam = getSymbol(methodParameters, s);
        if (maybeDefined.isPresent()) {
            reports.add(this.createErrorReport(jmmNode, "Variable Symbol is already defined as " +maybeDefined.get()));
        }
        if (maybeParam.isPresent()) {
            reports.add(this.createErrorReport(jmmNode, "Variable " + s + " is already defined in the parameter as " +maybeParam.get()));
        }
        // TODO: talk with bispo about this design decision: if it is wrong should we add it to the symbol table?
        //else {
        //    this.thisScope.addSymbol(s);
        //}
        this.thisScope.addSymbol(s);
        Optional<Symbol> maybeField = getSymbol(classFields, s);
        String shadowingText = "Variable " + s + "is shadowing ";
        maybeField.ifPresent(symbol -> reports.add(this.createWarningReport(jmmNode, shadowingText + "class field" + symbol)));
        return null;

    }

    public Optional<Symbol> getSymbol(List<Symbol> symbols, Symbol target) {
        for (Symbol s : symbols) {
            if (s.getName().equals(target.getName())) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public ScopeSymbolTable getScope() {
        return this.thisScope;
    }

}