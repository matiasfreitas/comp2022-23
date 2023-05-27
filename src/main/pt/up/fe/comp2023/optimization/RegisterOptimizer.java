package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.*;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class RegisterOptimizer {
    private final ClassUnit classUnit;
    private final Liveness livenessAnalyzer;

    public RegisterOptimizer(ClassUnit classUnit) {

        this.classUnit = classUnit;
        this.livenessAnalyzer = new Liveness();
    }

    public void allocateRegisters(int n) {

        try {

            classUnit.checkMethodLabels();
            classUnit.buildCFGs();
            classUnit.buildVarTables();
        } catch (OllirErrorException e) {

            e.printStackTrace();
            return;
        }
        for (Method method : classUnit.getMethods()) {

            ArrayList<HashMap<Node, BitSet>> liveRanges = livenessAnalyzer.liveness(method);
            GraphColouring  graph = new GraphColouring(liveRanges, method);
        }
        try {

            classUnit.checkMethodLabels();
            classUnit.buildCFGs();
            classUnit.buildVarTables();
        }catch (OllirErrorException e) {

            e.printStackTrace();
            return;
        }

    }

}