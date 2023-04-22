package pt.up.fe.comp2023.analysis.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class JmmSymbolTable implements SymbolTable {
    List<String> imports;
    ClassSymbolTable classSymbolTable;
    Map<String,MethodSymbolTable> methods;
    List<String> methodNames;

    public JmmSymbolTable(List<String> imports,ClassSymbolTable classSymbolTable){
        this.imports = imports;
        this.classSymbolTable = classSymbolTable;
        methodNames = new LinkedList<>();
        methods = new HashMap<>();
        for (MethodSymbolTable m : classSymbolTable.methods){
            String mRepr = m.getStringRepresentation();
            methodNames.add(mRepr);
            methods.put(mRepr,m);
        }
    }



    public Boolean isStaticMethod(String s){
        MethodSymbolTable m = methods.get(s);
        return m.isStatic();
    }
    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() {
        return classSymbolTable.name;
    }

    @Override
    public String getSuper() {
        return classSymbolTable.parentClass;
    }

    @Override
    public List<Symbol> getFields() {
        return classSymbolTable.classFields;
    }

    @Override
    public List<String> getMethods() {
        return methodNames;
    }

    @Override
    public Type getReturnType(String s) {
        MethodSymbolTable m = methods.get(s);
        return m.getReturnType();

    }

    @Override
    public List<Symbol> getParameters(String s) {
        MethodSymbolTable m = methods.get(s);
        return m.getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        MethodSymbolTable m = methods.get(s);
        ScopeSymbolTable mBody = m.getBodyScope();
        return mBody.flatten();
    }

}
