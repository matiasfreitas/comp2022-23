package pt.up.fe.comp2023.analysis.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ClassSymbolTable {
    public List<Symbol> classFields = new LinkedList<>();
    //private Map<String,MethodSymbolTable> methods = new HashMap<>();
    public List<MethodSymbolTable> methods = new LinkedList<>();
    public Boolean isStatic = false;
    public String parentClass;
    public String name;

    public void setIsStatic(boolean b) {
        this.isStatic = true;
    }

    public void setParentClass(String extendsName) {
        this.parentClass = extendsName;
    }

    public void addMethod(MethodSymbolTable methodSymbolTable) {
        this.methods.add(methodSymbolTable);
    }

    public void addField(Symbol s) {
        classFields.add(s);
    }

    public List<Symbol> getClassFields() {
        return this.classFields;
    }

    public Optional<Symbol> getSymbol(String name) {
        for (Symbol s : classFields) {
            if (s.getName().equals(name)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    public void setIsStatic(Boolean isStatic) {
        this.isStatic = isStatic;
    }

    public void setName(String className) {
        this.name = className;
    }

    public String tableToString(String identation) {
        String isStatic = this.isStatic ? "static " : "";
        String extendsClass = !(this.parentClass == null) ? "extends " + this.parentClass : "";
        String showFields = (this.classFields.size() > 0) ? "Fields:\n" : "";
        StringBuilder fields = new StringBuilder();
        String thisIdentation = identation + "  ";
        for (Symbol field : this.classFields) {
            fields.append(thisIdentation + "  ").append(field.toString()).append("\n");
        }
        StringBuilder methods = new StringBuilder();
        String showMethods = (this.methods.size() > 0) ? "Methods:\n" : "";
        for (MethodSymbolTable method : this.methods) {
            methods.append(method.tableToString(thisIdentation + "  "));
        }
        return identation + isStatic + "class " + this.name + extendsClass + "\n" +
                thisIdentation + showFields + fields + thisIdentation + showMethods + methods;


    }

}
