package pt.up.fe.comp2023;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2023.analysis.Analyser;
import pt.up.fe.comp2023.jasmin.JasminEngine;
import pt.up.fe.comp2023.ollir.OllirExpressionResult;
import pt.up.fe.comp2023.ollir.Optimization;
import pt.up.fe.comp2023.optimization.ConstantFolding;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsLogs;
import pt.up.fe.specs.util.SpecsSystem;

import static pt.up.fe.comp.TestUtils.getJasminBackend;

public class Launcher {

    public static void main(String[] args) {
        // Setups console logging and other things
        SpecsSystem.programStandardInit();

        // Parse arguments as a map with predefined options
        var config = parseArgs(args);

        // Get input file
        File inputFile = new File(config.get("inputFile"));

        // Check if file exists
        if (!inputFile.isFile()) {
            throw new RuntimeException("Expected a path to an existing input file, got '" + inputFile + "'.");
        }

        List<Report> compilationReports = compile(inputFile, config);
        printReports(compilationReports);

    }

    private static void printReports(List<Report> reports) {
        for (var report : reports) {
            System.err.println(report.toString());
        }
    }

    private static boolean hasErrors(List<Report> reports) {
        for (var report : reports) {
            if (report.getType().equals(ReportType.ERROR)) {
                return true;
            }
        }
        return false;
    }

    private static List<Report> compile(File jmm, Map<String, String> config) {
        String code = SpecsIo.read(jmm);
        // Instantiate JmmParser
        SimpleParser parser = new SimpleParser();
        // Parse stage
        JmmParserResult parserResult = parser.parse(code, config);
        // Check if there are parsing errors
        if (hasErrors(parserResult.getReports())) {
            return parserResult.getReports();
        }
        Analyser analyser = new Analyser();
        JmmSemanticsResult semanticsResult = analyser.semanticAnalysis(parserResult);
        if (hasErrors(semanticsResult.getReports())) {
            return semanticsResult.getReports();
        }

        Optimization optimizer = new Optimization();
        OllirResult ollirResult = optimizer.toOllir(semanticsResult);
        if (hasErrors(ollirResult.getReports())) {
            return ollirResult.getReports();
        }
        JasminBackend backend = getJasminBackend();
        JasminResult jasminResult = backend.toJasmin(ollirResult);
        jasminResult.compile(new File(ollirResult.getOllirClass().getClassName() + ".class"));
        return jasminResult.getReports();

    }

    private static Map<String, String> parseArgs(String[] args) {
        SpecsLogs.info("Executing with args: " + Arrays.toString(args));

        // Check if there is at least one argument
        if (args.length < 1) {
            throw new RuntimeException("Expected a single argument, a path to an existing input file.");
        }

        // Create config
        Map<String, String> config = new HashMap<>();
        config.put("inputFile", args[0]);
        config.put("optimize", optimizationsOn(args));
        config.put("registerAllocation", registerCount(args));
        config.put("debug", "false");



        return config;
    }

    private static String optimizationsOn(String[] args) {
        for (var arg : args) {
            if (arg.equals("-o"))
                return "true";
        }
        return "false";
    }

    private static String registerCount(String[] args) {
        for (var arg : args) {
            if (arg.startsWith("-r="))
                return arg.substring(3);
        }
        return "-1";
    }

}
