package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.optimization.ConstantFolding;
import org.specs.comp.ollir.*;
import pt.up.fe.comp2023.optimization.GraphColouring;
import pt.up.fe.comp2023.optimization.Liveness;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;


public class Optimization implements JmmOptimization {
    private int nRegisters;

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        ConstantFolding folder = new ConstantFolding();
        if (semanticsResult.getConfig().getOrDefault("optimize", "false").equals("true")) {
            return folder.optimize(semanticsResult);
        }
        return semanticsResult;
    }

    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {
        // TODO: Need to understand the pros and cons of var in java and auto in c++
        var optimizedSemanticResult = optimize(jmmSemanticsResult);
        var ollirGenerator = new OllirGenerator((JmmSymbolTable) jmmSemanticsResult.getSymbolTable());
        var rootNode = optimizedSemanticResult.getRootNode();
        var ollirReports = new LinkedList<Report>();
        var ollirCode = ollirGenerator.visit(rootNode, ollirReports);
        return new OllirResult(optimizedSemanticResult, ollirCode, ollirReports);
    }


    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        if (!ollirResult.getConfig().containsKey("registerAllocation")) {
            return ollirResult;
        }

        nRegisters = Integer.parseInt(ollirResult.getConfig().get("registerAllocation"));
        if (nRegisters == -1) return ollirResult;

        ClassUnit classUnit = ollirResult.getOllirClass();
        Liveness liveness = new Liveness();
        classUnit.buildCFGs();
        System.out.println("REGISTER ALLOCATION");
        for (Method method : classUnit.getMethods()) {
            System.out.println("\nMethod: " + method.getMethodName() + "\n");

            ArrayList<HashMap<Node, BitSet>> liveRanges = liveness.liveness(method);
            GraphColouring graph = new GraphColouring(liveRanges, method);
            boolean possible  = graph.KColoring(nRegisters);
            if (!possible) {
                System.out.println("Method " + method.getMethodName() + " needs at least " + + graph.getmRegisters() + " registers");
                ollirResult.getReports().add(new Report(ReportType.ERROR, Stage.OPTIMIZATION, -1, -1,
                        "Method " + method.getMethodName() + " needs at least " + graph.getmRegisters() + " registers"));
            }

            HashMap<String, Integer> registers = graph.getRegisters();
            HashMap<String, Descriptor> varTable = method.getVarTable();
            System.out.println(registers);
            for(String var : registers.keySet()){
                try {
                    varTable.get(var).setVirtualReg(registers.get(var));
                }
                catch (Exception e){

                }
            }
        }

        classUnit.buildCFGs();


        return ollirResult;
    }

}
