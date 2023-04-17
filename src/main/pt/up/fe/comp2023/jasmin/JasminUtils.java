package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.ArrayList;
import java.util.HashMap;


public class JasminUtils {

    public static String addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable) {

        StringBuilder code = new StringBuilder();

        switch(instruction.getInstType()){
            case ASSIGN :

                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                Operand op1                         = (Operand) assignInstruction.getDest();
                Type type                           = (Type) op1.getType();
                String prefix                       = "i";

                if (type.getTypeOfElement() == ElementType.OBJECTREF)
                    prefix = "a";

                code.append(prefix + "store " +  varTable.get(op1.getName()).getVirtualReg() + "\n");
                return code.toString();

            case CALL:


            case PUTFIELD:
            case GETFIELD:
            case RETURN:
                return "";
        }
        return "";
    }

    public static String jasminType(Type fieldType, ArrayList<String> imports) throws Exception {
        switch (fieldType.getTypeOfElement()) {
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case ARRAYREF:
                return "a";
            case OBJECTREF:
                ClassType classType = (ClassType) fieldType;
                for (String statement : imports) {
                    String[] importArray = statement.split("\\.");
                    if (importArray[importArray.length - 1].equals(classType.getName())) {
                        return statement.replace('.', '/');
                    }
                }
            case VOID:
                return "V";
            case INT32:
                return "I";
            default:
                throw new Exception(String.valueOf(fieldType));
        }
    }
}