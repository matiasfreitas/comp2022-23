package pt.up.fe.comp;

import org.junit.Test;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;
import pt.up.fe.comp2023.ollir2.OllirGenerator;
import pt.up.fe.specs.util.SpecsIo;

import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

public class MyOllirTest {

    static OllirResult getOllirResult(String filename) {
        return new OllirResult(SpecsIo.getResource("pt/up/fe/comp/ollir/" + filename), new HashMap<>());
    }


    public void testOllir(String path){
        String jmmCode= SpecsIo.getResource(path);
        var output = TestUtils.analyse(jmmCode);
        var rootNode = output.getRootNode();
        System.out.println(rootNode.toTree());
        var semanticReports = output.getReports();
        if(semanticReports.size() > 0){
            System.out.println("Reports:");
            boolean error = false;
            for(Report r : semanticReports){
                error  = error || r.getType().equals(ReportType.ERROR);
                System.err.println(r.toString());
            }
            if(error){
                System.err.println("Errors during semantic analysis aborting ollir");
                return;
            }
        }

        var ollirGenerator = new OllirGenerator((JmmSymbolTable) output.getSymbolTable());
        var reports = new LinkedList<Report>();
        var ollirCode = ollirGenerator.visit(rootNode,reports);
        System.out.println("Ollir Code:\n"+ ollirCode);
    }

    @Test
    public void idTypeBug(){
        var path = "pt/up/fe/comp/myollir/idTypeBug.jmm";
        testOllir(path);
    }

    @Test
    public void helloWorld(){
        var path = "pt/up/fe/comp/myollir/helloWorld.jmm";
        testOllir(path);
    }



}
