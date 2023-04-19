package pt.up.fe.comp2023.jasmin;
import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;

import static org.specs.comp.ollir.ElementType.OBJECTREF;
import static org.specs.comp.ollir.ElementType.VOID;

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
        if (isStatic) code.append("static ");
        if (isFinal) code.append("final ");
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
            code.append(".super ");

            for (String statement : this.classUnit.getImports()) {
                String[] importArray = statement.split("\\.");

                if (importArray[importArray.length - 1].equals(superClass)) {
                    code.append(statement.replace('.', '/'));
                    break;
                }
            }
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
            code.append("invokespecial ");
            for (String statement : this.classUnit.getImports()) {
                String[] importArray = statement.split("\\.");

                if (importArray[importArray.length - 1].equals(classUnit.getSuperClass())) {
                    code.append(statement.replace("\\.", "/"));

                    break;
                }
            }
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
                    code.append(JasminUtils.jasminType(param.getType(), classUnit.getImports()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            code.append(")");
            try {

                ArrayList<String> imports = classUnit.getImports();
                imports.add(classUnit.getClassName());
                if (method.getReturnType().getTypeOfElement() == OBJECTREF)
                    code.append("L");
                code.append(JasminUtils.jasminType(method.getReturnType(), imports) + "\n");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            code.append(".limit stack 99\n");
            code.append(".limit locals 99\n");

            for (Instruction instruction: method.getInstructions()) {
                addInstruction(instruction, method.getVarTable());
            }

            code.append(".end method\n");
        }

    }

    public void addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {

        ArrayList<String> imports = classUnit.getImports();
        imports.add(classUnit.getClassName());
        code.append(JasminUtils.addInstruction(instruction, varTable, imports).replace("Dummy", classUnit.getClassName()));
    }
}