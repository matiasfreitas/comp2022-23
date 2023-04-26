package pt.up.fe.comp2023.analysis.semantics;

import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

public class UsageContext {

    private Boolean classContext;
    private String methodSignature;

    public UsageContext() {
        this.classContext = false;
    }

    public void setClassContext() {
        this.classContext = true;
    }

    public Boolean isClassContext() {
        return this.classContext;
    }

    public void setMethodContext(String methodSignature) {
        this.methodSignature = methodSignature;
        this.classContext = false;
    }

    public String getMethodSignature() {
        return this.methodSignature;
    }
}
