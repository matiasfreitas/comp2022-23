package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;

import java.util.List;

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


    public static boolean anyOptimization(List<JmmIterativeOptimizer> optimizers) {
        for (var optimizer : optimizers) {
            if (optimizer.didAnyOptimization()) {
                return true;
            }
        }
        return false;
    }

}
