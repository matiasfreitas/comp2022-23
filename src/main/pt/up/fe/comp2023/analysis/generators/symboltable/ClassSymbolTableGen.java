package pt.up.fe.comp2023.analysis.generators.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.generators.SymbolGen;
import pt.up.fe.comp2023.analysis.semantics.Analyser;
import pt.up.fe.comp2023.analysis.symboltable.ClassSymbolTable;
import pt.up.fe.comp2023.analysis.symboltable.MethodSymbolTable;

import java.util.List;
import java.util.Optional;

public class ClassSymbolTableGen extends Analyser<Void> {
    ClassSymbolTable classTable;

    public ClassSymbolTableGen(JmmNode root) {
        super(root);
        classTable = new ClassSymbolTable();
    }

    @Override
    protected void buildVisitor() {
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("ClassVarDeclaration", this::handleVarClassDeclaration);

    }

    private Void handleVarClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        //System.out.println("Looking at a Class Var Declaration");
        // TODO: Handle Visibility
        // TODO: Is the type of the symbol available
        String visibility = "private";
        if (jmmNode.hasAttribute("visibility")) {
            visibility = jmmNode.get("visibility");
        }
        SymbolGen sGen = new SymbolGen();
        sGen.visit(jmmNode);
        Symbol s = sGen.getSymbol();
        Optional<Symbol> isDefined = this.classTable.getSymbol(s.getName());
        if (isDefined.isPresent()) {
            Symbol alreadyDefined = isDefined.get();
            reports.add(this.createReport(jmmNode, "Class Symbol is already defined as " + alreadyDefined));
        } else {
            classTable.addField(s);
        }
        return null;
    }

    private Void handleClassDeclaration(JmmNode jmmNode, List<Report> reports) {
        // System.out.println("Handling Class");
        if (jmmNode.hasAttribute("extendsName")) {
            classTable.setParentClass(jmmNode.get("extendsName"));
        }
        if (jmmNode.hasAttribute("isStatic")) {
            classTable.setIsStatic(true);
        }
        classTable.setName(jmmNode.get("className"));
        this.visitAllChildren(jmmNode, reports);
        return null;
    }

    private Void handleMethodDeclaration(JmmNode jmmNode, List<Report> reports) {
        MethodSymbolTableGen methodTableGen = new MethodSymbolTableGen(jmmNode,classTable.getClassFields());
        List<Report> methodReports = methodTableGen.analyse();
        reports.addAll(methodReports);
        MethodSymbolTable methodSymbolTable = methodTableGen.getMethodTable();
        methodSymbolTable.setParentClass(this.classTable);
        // TODO: method redefinition?
        this.classTable.addMethod(methodSymbolTable);
        return null;
    }

    public ClassSymbolTable getClassTable() {
        return this.classTable;
    }


}
