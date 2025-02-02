package pt.up.fe.comp2023.jasmin;
import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.specs.comp.ollir.ElementType.*;

public class OllirToJasmin {

    private final ClassUnit classUnit;
    private StringBuilder code;
    public OllirToJasmin(ClassUnit classUnit) {
        this.classUnit = classUnit;
        this.code      = new StringBuilder("");
    }

    public String jasminCode() {

        addClass();
        addFields();
        addMethods();

        return code.toString();
    }

    public void addAccessModifiers(String info, Boolean isStatic, Boolean isFinal) {
        if (info != "DEFAULT")
            code.append(info.toLowerCase() + " ");
        if (info.equals("DEFAULT")) code.append("public ");
        if (isStatic) code.append("static ");
        if (isFinal)  code.append("final ");
    }
    public void addClass() {


        String prefix = (classUnit.getPackage() != null) ? classUnit.getPackage() : "";
        if (prefix != "") {
            prefix += "/";
        }
        String classInfo  = classUnit.getClassAccessModifier().name();
        String superClass = classUnit.getSuperClass();
        String className  = prefix + classUnit.getClassName();

        code.append(".class ");
        addAccessModifiers(classInfo, classUnit.isStaticClass(), classUnit.isFinalClass());
        code.append(className + '\n');


        if (superClass != null) {
            code.append(".super " + superClass + "\n");
        }
        else code.append(".super java/lang/Object");

        code.append('\n');
    }

    public void addFields() {

        for (Field field: classUnit.getFields()) {
            addField(field);
        }
    }

    public void addField(Field field) {

        code.append(".field ");
        addAccessModifiers(field.getFieldAccessModifier().name(), field.isStaticField(), field.isFinalField());
        code.append(field.getFieldName() + " ");
        try {
            if (field.isInitialized())
                code.append("=" + field.getInitialValue());
            code.append(JasminUtils.jasminType(field.getFieldType(), classUnit.getImports()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        code.append('\n');

    }
    public void addMethods() {

        for (Method method : classUnit.getMethods()) {
            method.buildVarTable();
            addMethod(method);
        }
    }

    public void addConstructor() {
        code.append(".method public <init>()V\n");
        code.append("aload_0\n");

        if (classUnit.getSuperClass() != null) {
            code.append("invokespecial " + classUnit.getSuperClass());

        }
        else {
            code.append("invokespecial java/lang/Object");
        }

        code.append("/<init>()V\n");
        code.append("return\n");
        code.append(".end method\n");
    }
    public void addMethod(Method method) {

        if (method.isConstructMethod()) addConstructor();
        else {
            code.append(".method ");
            addAccessModifiers(method.getMethodAccessModifier().name(), method.isStaticMethod(), method.isFinalMethod());
            code.append(method.getMethodName());
            code.append("(");

            for (Element param: method.getParams()) {
                try {
                    ArrayList<String> imports = classUnit.getImports();
                    imports.add(classUnit.getClassName());
                    code.append(JasminUtils.jasminType(param.getType(), imports));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            code.append(")");
            try {

                ArrayList<String> imports = classUnit.getImports();
                imports.add(classUnit.getClassName());
                code.append(JasminUtils.jasminType(method.getReturnType(), imports));
                code.append("\n");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            StringBuilder body = new StringBuilder();

            for (Instruction instruction: method.getInstructions()) {
                var labels = method.getLabels(instruction);
                for (String label : labels) {
                    body.append(label + ":\n");
                }
                body.append(addInstruction(instruction, method.getVarTable()));
            }

            code.append(".limit stack " + JasminUtils.stackLimit + "\n");
            code.append(".limit locals " + methodLocalLimit(method) +  "\n");

            code.append(body);
            code.append(".end method\n");

            JasminUtils.resetLimit();
        }

    }

    public int methodLocalLimit(Method method) {
        ArrayList<Integer> methodRegs = new ArrayList<>();

        methodRegs.add(0); // Class Register

        for (var descriptor : method.getVarTable().values()) {
            if (!methodRegs.contains(descriptor.getVirtualReg()))
                methodRegs.add(descriptor.getVirtualReg());
        }

        return methodRegs.size();
    }


    public String addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {

        ArrayList<String> imports = classUnit.getImports();
        imports.add(classUnit.getClassName());
        return (JasminUtils.addInstruction(instruction, varTable, imports).replace("Dummy", classUnit.getClassName()));
    }
}