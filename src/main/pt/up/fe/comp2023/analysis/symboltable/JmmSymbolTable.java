package pt.up.fe.comp2023.analysis.symboltable;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class JmmSymbolTable implements SymbolTable {
    List<String> imports;

    List<Type> importTypes;
    ClassSymbolTable classSymbolTable;
    Map<String, MethodSymbolTable> methods;
    List<String> methodNames;

    public JmmSymbolTable(List<String> imports, ClassSymbolTable classSymbolTable) {
        this.imports = imports;
        this.importTypes = getImportTypes(imports);
        this.classSymbolTable = classSymbolTable;
        methodNames = new LinkedList<>();
        methods = new HashMap<>();
        for (MethodSymbolTable m : classSymbolTable.methods) {
            String mRepr = m.getStringRepresentation();
            methodNames.add(mRepr);
            methods.put(mRepr, m);
        }
    }
    private List<Type> getImportTypes(List<String> imports){
       List<Type> types = new ArrayList<>();
       for(String i : imports){
           String[] parts = i.split("[.]");
           String typeName = parts[parts.length -1];
           types.add(new Type(typeName,false));
        }
        return types;
    }

    public Optional<Type> getFieldTry(String t) {
        for (Symbol s : this.getFields()) {
            if (s.getName().equals(t)) {
                return Optional.ofNullable(s.getType());
            }
        }
        return Optional.empty();
    }

    public Boolean isStaticMethod(String s) {
        MethodSymbolTable m = methods.get(s);
        return m.isStatic();
    }

    public Boolean isThisClassType(String t) {
        return t.equals(this.getClassName());
    }

    public Boolean isImportedSymbol(String s) {
        String last = '.' + s;
        for (String module : imports) {
            if (module.endsWith(last) || module.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public List<Type> getImportTypes(){
        return  this.importTypes;
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
        if (m == null) {
            return  null;
        }
        return m.getReturnType();

    }

    @Override
    public List<Symbol> getParameters(String s) {
        MethodSymbolTable m = methods.get(s);
        if(m == null){
            return  null;
        }
        return m.getParameters();
    }

    @Override
    public List<Symbol> getLocalVariables(String s) {
        MethodSymbolTable m = methods.get(s);
        if(m == null){
            return  null;
        }
        ScopeSymbolTable mBody = m.getBodyScope();
        return mBody.flatten();
    }

    public List<MethodSymbolTable> getOverloads(String methodRepresentation) {
        List<MethodSymbolTable> s = new LinkedList<>();
        this.methods.forEach((k, v) -> {
            v.isOverload(methodRepresentation);
        });
        return s;

    }
}
