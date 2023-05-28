package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

public abstract class JmmIterativeOptimizer extends AJmmVisitor<Void, Void> {
    private boolean optimized = false;

    public boolean didAnyOptimization() {
        return optimized;
    }

    public void didOptimize() {
        optimized = true;
    }

    public void startOptimizing() {
        optimized = false;
    }

    public abstract JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult);

}
