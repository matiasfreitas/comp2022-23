package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.LinkedList;
import java.util.List;

public class MethodSymbolTable {
    private Type returnType;
    private String name;
    private List<Symbol> parameters = new LinkedList<>();
    private ClassSymbolTable parentClass;
    private ScopeSymbolTable methodScope;
    private String visibility;
    private Boolean isStatic;

    public void setParentClass(ClassSymbolTable parent) {
        this.parentClass = parent;
    }

    public String getStringRepresentation() {
        return "Lara my bomboca";
    }

    public void setMethodScope(ScopeSymbolTable methodScope) {
        this.methodScope = methodScope;
    }

    public void addParameter(Symbol p){
        this.parameters.add(p);
    }
    public void setMethodName(String methodName) {
        this.name = methodName;
    }

    public void setIsStatic(Boolean isStatic) {
        this.isStatic = isStatic;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String tableToString(String indentation) {

        String isStatic = this.isStatic ? "static" : "";
        String visibility = this.visibility;
        StringBuilder parameters = new StringBuilder();
        String thisIndentation = indentation + "  ";
        for (Symbol field : this.parameters) {
            parameters.append(field.toString()).append(", ");
        }
        return indentation + visibility + " " + isStatic + " " + this.name + "\n" +
                thisIndentation + "Parameters: (" + parameters + ")\n" +
                thisIndentation + "Method Body:\n" + this.methodScope.tableToString(thisIndentation + "  ");


    }
}
