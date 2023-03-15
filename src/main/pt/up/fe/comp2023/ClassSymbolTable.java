package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Symbol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassSymbolTable {
    private List<Symbol> classFields = new LinkedList<>();
    //private Map<String,MethodSymbolTable> methods = new HashMap<>();
    List<MethodSymbolTable> methods = new LinkedList<>();
    private Boolean isStatic = false;
    private String parentClass;
    private String name;

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
    public void setIsStatic(Boolean isStatic){
        this.isStatic = isStatic;
    }

    public void setName(String className) {
        this.name = className;
    }
    public String tableToString(String identation) {
        String isStatic = this.isStatic? "static " : "";
        String extendsClass = !(this.parentClass == null) ? "extends " + this.parentClass : "";
        String showFields = (this.classFields.size() >0)?  "Fields:\n" : "";
        StringBuilder fields = new StringBuilder();
        for(Symbol field : this.classFields){
            fields.append(identation).append(field.toString()).append("\n");
        }
        StringBuilder methods = new StringBuilder();
        for(MethodSymbolTable method : this.methods){
            methods.append(method.tableToString(identation + "  "));
        }
        return identation + isStatic + "class " + this.name + extendsClass + "\n" +
            identation + showFields  + fields  + methods;


    }

}
