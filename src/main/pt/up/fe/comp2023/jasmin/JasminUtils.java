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

                code.append(addInstruction(assignInstruction.getRhs(), varTable));
                code.append(prefix + "store " +  varTable.get(op1.getName()).getVirtualReg() + "\n");
                return code.toString();

            case BINARYOPER:

                BinaryOpInstruction binaryInstruction = (BinaryOpInstruction) instruction;
                Operand left                          = (Operand) binaryInstruction.getLeftOperand();
                Operand right                         = (Operand) binaryInstruction.getRightOperand();
                OperationType opType                  = binaryInstruction.getOperation().getOpType();

                code.append("iload " +  varTable.get(left.getName()).getVirtualReg() + "\n");
                code.append("iload " +  varTable.get(right.getName()).getVirtualReg() + "\n");

                switch (opType) {

                    case ADD: code.append("iadd\n"); break;
                    case SUB: code.append("isub\n"); break;
                    case MUL: code.append("imul\n"); break;
                    case DIV: code.append("idiv\n"); break;
                    default:
                        code.append("\n");
                }

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
            case VOID:
                return "V";
            case INT32:
                return "I";

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
                for (String statement : imports) {
                    String[] importArray = statement.split("\\.");
                    if (importArray[importArray.length - 1].equals(objectClass)) {
                        return "[".repeat(dimensions) + statement.replace('.', '/');
                    }
                }

                if (fieldType instanceof ArrayType) {
                    Type newFieldType = new Type(((ArrayType) fieldType).getElementType().getTypeOfElement());
                    return "[".repeat(dimensions) + jasminType(newFieldType, imports);
                }

            default:
                throw new Exception(String.valueOf(fieldType));
        }
    }
}