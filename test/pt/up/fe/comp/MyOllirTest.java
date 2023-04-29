package pt.up.fe.comp;

import org.junit.Test;
import org.specs.comp.ollir.ArrayType;
import org.specs.comp.ollir.ClassType;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Type;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.ollir2.OllirGenerator;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

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


    @Test
    public void testHelloWorld(){
        String jmmCode= SpecsIo.getResource("pt/up/fe/comp/myollir/helloWorld.jmm");
        var output = TestUtils.parse(jmmCode);
        var rootNode = output.getRootNode();
        System.out.println(rootNode.toTree());

        var ollirGenerator = new OllirGenerator();
        var reports = new LinkedList<Report>();
        var ollirCode = ollirGenerator.visit(rootNode,reports);
        System.out.println("Ollir Code:\n"+ ollirCode);

    }

    public String getType(Type type) {
        if (type.getTypeOfElement() == ElementType.OBJECTREF) {
            var classType = (ClassType) type;
            return classType.getName();
        }

        if (type.getTypeOfElement() == ElementType.ARRAYREF) {
            var arrayType = (ArrayType) type;
            System.out.println("TYPE OF ELEMENT: " + arrayType.getTypeOfElement());
            System.out.println("TYPE OF ELEMENTS: " + arrayType.getTypeOfElements());
            return arrayType.getTypeOfElements().toString();
        }

        System.out.println("Not yet implemented: " + type.getTypeOfElement());
        return type.toString();
    }

    @Test
    public void testMyclass1() {
        var result = getOllirResult("myclass1.ollir");

        // result.getOllirClass().get

        var methodName = getOllirResult("myclass1.ollir")
                        .getOllirClass().getMethod(1).getMethodName();
        assertEquals("sum", methodName);
    }



}
