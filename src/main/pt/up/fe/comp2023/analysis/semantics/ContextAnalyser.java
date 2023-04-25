package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class ContextAnalyser<T>  extends Analyser<T> {
    protected JmmSymbolTable symbolTable;

    protected UsageContext context;

    protected List<Type> availableTypes;

    public ContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root);
        this.symbolTable = symbolTable;
        this.context = context;
        this.availableTypes = new ArrayList<>();
        availableTypes.addAll(JmmBuiltins.builtinTypes());
        availableTypes.addAll(symbolTable.getImportTypes());
        availableTypes.add(new Type(symbolTable.getClassName(),false));


    }


    private Optional<Type> checkClassScope(String identifier) {
        return symbolTable.getFieldTry(identifier);

    }

    private Optional<Type> checkImports(String identifier) {
        if (symbolTable.isImportedSymbol(identifier)) {
            return Optional.of(new Type(identifier, false));
        }
        return Optional.empty();
    }

    private Optional<Type> checkUpperScopes(String identifier) {
        Optional<Type> t = checkClassScope(identifier);
        if (t.isEmpty())
            t = checkImports(identifier);
        return t;

    }

    protected boolean validType(Type t){
        Type compareT = t;
        if (t.isArray()){
            compareT = new Type(t.getName(),false);
        }
        for(Type available:availableTypes){
            if(compareT.equals(available)){
                return  true;
            }
        }
        return false;
    }

    protected Void handleVarDeclaration(JmmNode jmmNode, List<Report> reports) {
        SymbolGen sg = new SymbolGen();
        sg.visit(jmmNode);
        Symbol s = sg.getSymbol();
        if (!this.validType(s.getType())) {
            reports.add(createReport(jmmNode, "Type " + s.getType() + " is not an available type"));
        }
        return null;
    }
    public Optional<Type> checkIdentifier(String identifier, JmmNode jmmNode, List<Report> reports) {
        Optional<Type> t;
        if (context.isClassContext()) {
            t = checkUpperScopes(identifier);
        }
        // Method context
        else {
            String currentMethod = context.getMethodSignature();
            for (Symbol s : symbolTable.getLocalVariables(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            for (Symbol s : symbolTable.getParameters(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            if (symbolTable.isStaticMethod(currentMethod)) {
                // Check statics?
                t = checkImports(identifier);
                if (t.isEmpty()) {
                    Optional<Type> accessingField = checkClassScope(identifier);
                    if (accessingField.isPresent()) {
                        reports.add(this.createReport(jmmNode, "Trying to acess non static field "+ identifier+" in static method"));
                        return t;
                    }
                }
            } else {
                t = checkUpperScopes(identifier);
            }
        }
        if (t.isEmpty()) {
            reports.add(this.createReport(jmmNode, "Undefined Identifier"));
        }
        return t;
    }

}
