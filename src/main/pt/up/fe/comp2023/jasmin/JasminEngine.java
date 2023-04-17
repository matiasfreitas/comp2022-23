package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.io.File;
import java.util.Collections;

public class JasminEngine implements JasminBackend {

    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        String jasminCode = new OllirToJasmin(ollirResult.getOllirClass()).jasminCode();
        var jasminResult = new JasminResult(jasminCode);
        jasminResult.compile();

        return new JasminResult(ollirResult, jasminCode, Collections.emptyList());

    }
}