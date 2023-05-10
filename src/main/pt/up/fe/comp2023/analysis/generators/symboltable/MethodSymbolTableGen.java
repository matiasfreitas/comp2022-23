package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.generators.TypeGen;
import pt.up.fe.comp2023.analysis.semantics.Analyser;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.ScopeSymbolTable;

import java.util.List;
import java.util.Optional;

public class MethodSymbolTableGen extends Analyser<Void> {
    MethodSymbolTable methodTable;

    List<Symbol> classFields;
    public MethodSymbolTableGen(JmmNode root ,List<Symbol> classFields){
        super(root);
        this.methodTable = new MethodSymbolTable();
        this.classFields = classFields;
    }
    @Override
    protected void buildVisitor() {
        addVisit("MethodDeclaration",this::handleMethodDeclaration);
        addVisit("MethodBody",this::handleMethodBody);
        addVisit("MethodArguments",this::handleMethodArguments);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleMethodArguments(JmmNode jmmNode, List<Report> reports) {
        for(JmmNode child : jmmNode.getChildren()) {
            SymbolGen sGen = new SymbolGen();
            sGen.visit(child);
            Symbol thisParameter = sGen.getSymbol();

            Optional<Symbol> alreadyDefined = this.methodTable.getParameter(thisParameter.getName());
            if(alreadyDefined.isPresent()){
                reports.add(this.createErrorReport(jmmNode, "Redefinition of parameter " + alreadyDefined.get()));
            }
            else {
                this.methodTable.addParameter(thisParameter);
            }
            Optional<Symbol> shadowsClassField = getSymbol(classFields,thisParameter);
            String shadowingText = "Parameter " +thisParameter + "is shadowing ";
            shadowsClassField.ifPresent(symbol -> reports.add(this.createWarningReport(jmmNode, shadowingText + "class field" + symbol)));
        }
        return null;
    }

    private Void handleMethodBody(JmmNode jmmNode, List<Report>reports) {
        //System.out.println("Handling Method Body");
        ScopeSymbolTableGen scopeTableGen = new ScopeSymbolTableGen(jmmNode,classFields, methodTable.getParameters());
        List<Report> scopeReports = scopeTableGen.analyse();
        reports.addAll(scopeReports);
        ScopeSymbolTable methodScope = scopeTableGen.getScope();
        methodTable.setMethodScope(methodScope);
        return null;
    }
    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        String visibility = "private";
        boolean isStatic = false;
        if(jmmNode.hasAttribute("visibility")){
            visibility = jmmNode.get("visibility");
        }
        if(jmmNode.hasAttribute("isStatic"))
            isStatic = true;

        String methodName = jmmNode.get("methodName");
        //System.out.println("Method " + methodName + " isStatic " + isStatic +" visibility " + visibility);
        this.methodTable.setMethodName(methodName);
        this.methodTable.setIsStatic(isStatic);
        this.methodTable.setVisibility(visibility);
        for (JmmNode child : jmmNode.getChildren()){
            if(child.getKind().equals("Type")) {
                TypeGen typeGen = new TypeGen();
                typeGen.visit(child);
                this.methodTable.setReturnType(typeGen.getType());
            }
            else{
                visit(child,reports);
            }
        }
        String signature = this.methodTable.getStringRepresentation();
        jmmNode.put("signature",signature);
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

    public MethodSymbolTable getMethodTable() {
        return  this.methodTable;
    }
}
