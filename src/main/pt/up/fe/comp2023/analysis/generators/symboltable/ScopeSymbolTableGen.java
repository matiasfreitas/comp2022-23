package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.semantics.Analyser;
import pt.up.fe.comp2023.analysis.symboltable.ScopeSymbolTable;

import java.util.List;
import java.util.Optional;

public class ScopeSymbolTableGen extends Analyser<Void> {
    ScopeSymbolTable thisScope;
    List<Symbol> classFields;
    List<Symbol> methodParameters;

    public ScopeSymbolTableGen(JmmNode root,List<Symbol>classFields,List<Symbol>methodParameters) {
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
        // System.out.println("Handling Var Declaration inside a scope");
        SymbolGen symbolGen = new SymbolGen();
        symbolGen.visit(jmmNode);
        Symbol s = symbolGen.getSymbol();
        //System.out.println(s.toString());
        Optional<Symbol> maybeDefined = this.thisScope.getSymbol(s.getName());
        if (maybeDefined.isPresent()) {
            Symbol alreadyDefined = maybeDefined.get();
            reports.add(this.createReport(jmmNode, "Variable Symbol is already defined as " + alreadyDefined.toString()));

        } else {
            this.thisScope.addSymbol(s);
        }
        return null;

    }

    public ScopeSymbolTable getScope() {
        return this.thisScope;
    }

}