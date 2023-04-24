package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class Analyser<T> extends PostorderJmmVisitor<List<Report>, T> {
    protected JmmSymbolTable symbolTable;
    protected JmmNode root;

    protected UsageContext context;

    public Analyser(JmmNode root, JmmSymbolTable symbolTable, UsageContext context) {
        this.root = root;
        this.symbolTable = symbolTable;
        this.context = context;
        this.setDefaultVisit((a, b) -> {
                    return null;
                }
        );
    }

    public List<Report> analyse() {
        List<Report> reports = new LinkedList<>();
        this.visit(this.root, reports);
        return reports;
    }

    protected Report createReport(JmmNode node, String message) {
        int line = Integer.parseInt(node.get("lineStart"));
        int column = Integer.parseInt(node.get("colStart"));
        return Report.newError(Stage.SEMANTIC, line, column, message, null);

    }

    private Optional<Type> checkUpperScopes(String identifier) {
        Optional<Type> classField = symbolTable.getFieldTry(identifier);
        if (classField.isEmpty()) {
            if (symbolTable.isImportedSymbol(identifier)) {
                return Optional.of(new Type(identifier, false));
            }
        }
        return classField;
    }

    public Optional<Type> checkIdentifier(String identifier, JmmNode jmmNode, List<Report> reports) {
        Optional<Type> t = Optional.empty();
        if (context.isClassContext()) {
            t = checkUpperScopes(identifier);
        }
        // Method context
        else {
            String currentMethod = context.getMethodSignature();
            for (Symbol s : symbolTable.getParameters(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            for (Symbol s : symbolTable.getLocalVariables(currentMethod)) {
                if (s.getName().equals(identifier)) {
                    return Optional.ofNullable(s.getType());
                }
            }
            t = checkUpperScopes(identifier);
        }
        if (t.isEmpty()) {
            reports.add(this.createReport(jmmNode, "Undefined Identifier"));
        }
        return t;
    }

}
