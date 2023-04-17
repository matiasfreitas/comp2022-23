package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

public class JasminUtils {

    public static String addInstruction(Instruction instruction) {
        // Generate Instructions Here

        switch(instruction.getInstType()){
            case ASSIGN :
            case CALL:
            case PUTFIELD:
            case GETFIELD:
            case GOTO:
            case RETURN:
                return "";
        }
        return "";
    }

    public static String jasminType(Type fieldType) throws Exception {
        switch (fieldType.getTypeOfElement()) {
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case ARRAYREF:
            case OBJECTREF:
                return "a";
            case VOID:
                return "V";
            case INT32:
                return "I";
            default:
                throw new Exception(String.valueOf(fieldType));
        }
    }
}