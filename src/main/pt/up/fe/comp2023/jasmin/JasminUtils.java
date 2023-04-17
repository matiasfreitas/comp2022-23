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
            case OBJECTREF:
                String objectClass;
                Integer dimensions;

                if (fieldType instanceof ArrayType) {
                    dimensions = ((ArrayType) fieldType).getNumDimensions();
                    objectClass = ((ArrayType) fieldType).getElementClass();
                }
                else {
                    objectClass = ((ClassType) fieldType).getName();
                    dimensions = 0;
                }
                System.out.println(imports);
                for (String statement : imports) {
                    String[] importArray = statement.split("\\.");
                    System.out.println(importArray[importArray.length - 1]);
                    if (importArray[importArray.length - 1].equals(objectClass)) {
                        return "[".repeat(1) + statement.replace('.', '/');
                    }
                }

                if (fieldType instanceof ArrayType) {

                    Type newFieldType = new Type(((ArrayType) fieldType).getElementType().getTypeOfElement());
                    return "[".repeat(1) + jasminType(newFieldType, imports);
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