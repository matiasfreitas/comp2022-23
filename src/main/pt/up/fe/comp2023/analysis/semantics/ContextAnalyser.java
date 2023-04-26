package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.JmmBuiltins;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ContextAnalyser<T> extends Analyser<T> {
    protected JmmSymbolTable symbolTable;

    protected UsageContext context;

    protected List<Type> availableTypes;

    public ContextAnalyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        super(root);
        this.symbolTable = symbolTable;
        this.context = context;
        this.availableTypes = new ArrayList<>();
        availableTypes.addAll(JmmBuiltins.builtinTypes());
        availableTypes.add(new Type(symbolTable.getClassName(), false));
    }


    private Optional<Type> checkClassScope(String identifier) {
        return symbolTable.getFieldTry(identifier);

    }

    private Optional<Type> checkMethodScope(String identifier, String currentMethod) {
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
        return Optional.empty();
    }

    private Optional<Type> checkImports(String identifier) {
        if (symbolTable.isImportedSymbol(identifier)) {
            return Optional.of(new Type(identifier, false));
        }
        return Optional.empty();
    }

    protected boolean validType(Type t) {
        // TODO: validType also increments usage :: bad Design ->> side effects
        Type compareT = t;
        if (t.isArray()) {
            compareT = new Type(t.getName(), false);
        }
        for (Type available : availableTypes) {
            if (compareT.equals(available)) {
                return true;
            }
        }
        // Should this function increment?
        for (Type importedType : symbolTable.getImportTypes()) {
            if (compareT.equals(importedType)) {
                symbolTable.incrementImport(importedType);
                return true;
            }
        }
        return false;
    }

    protected Void handleVarDeclaration(JmmNode jmmNode, List<Report> reports) {
        SymbolGen sg = new SymbolGen();
        sg.visit(jmmNode);
        Symbol s = sg.getSymbol();
        if (!this.validType(s.getType())) {
            reports.add(createErrorReport(jmmNode, "Type " + s.getType() + " is not an available type"));
        }
        return null;
    }

    private Optional<Type> handleIdentifierInMethod(String identifier, String currentMethod, JmmNode jmmNode, List<Report> reports) {
        Optional<Type> inMethod = checkMethodScope(identifier, currentMethod);
        if (inMethod.isPresent()) {
            return inMethod;
        }
        Optional<Type> inClass = checkClassScope(identifier);
        if (inClass.isPresent()) {
            if (symbolTable.isStaticMethod(currentMethod)) {
                // For now we assume that this is an error but we could try to check for imports to see if something is available
                reports.add(this.createErrorReport(jmmNode, "Trying to acess non static field " + identifier + " in static method"));
                return Optional.empty();
            }
            return inClass;
        }
        return Optional.empty();
    }

    public Optional<Type> checkIdentifier(String identifier, JmmNode jmmNode, List<Report> reports) {
        if (!context.isClassContext()) {
            Optional<Type> inMethod = handleIdentifierInMethod(identifier, context.getMethodSignature(), jmmNode, reports);
            if (inMethod.isPresent())
                return inMethod;
        }
        // Class Context
        else {
            Optional<Type> inClass = checkClassScope(identifier);
            if (inClass.isPresent())
                return inClass;
        }
        // Could be an import
        Type identifierType = new Type(identifier, false);
        if (!validType(identifierType)) {
            reports.add(this.createErrorReport(jmmNode, "Undefined Identifier " + identifier));
            return Optional.empty();
        }
        return Optional.of(identifierType);
    }

}
