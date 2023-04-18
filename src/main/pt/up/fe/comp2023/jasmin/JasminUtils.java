package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.HashMap;


public class JasminUtils {

    public static String addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code = new StringBuilder();
        Operand object;
        Operand field;
        String prefix;

        switch(instruction.getInstType()){
            case ASSIGN :

                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                Operand op1                         = (Operand) assignInstruction.getDest();
                Type type                           = (Type) op1.getType();
                prefix                              = "i";

                if (type.getTypeOfElement() == ElementType.OBJECTREF)
                    prefix = "a";

                code.append(addInstruction(assignInstruction.getRhs(), varTable, imports));
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

                StringBuilder invokeInstruction = new StringBuilder();
                CallInstruction callInstruction = (CallInstruction) instruction;
                object                          = (Operand) callInstruction.getFirstArg();
                LiteralElement method           = (LiteralElement) callInstruction.getSecondArg();
                String methodName;

                if (callInstruction.getInvocationType() == CallType.NEW) {
                    invokeInstruction.append("invokespecial " );
                    code.append("new " +  jasminType(callInstruction.getFirstArg().getType(), imports) + "\ndup\n");
                    methodName = "<init>";
                }
                else if (callInstruction.getInvocationType() == CallType.invokestatic) {

                    methodName = method.getLiteral();
                    invokeInstruction.append("invokestatic " + object.getName() + "/");
                }
                else {
                    invokeInstruction.append(callInstruction.getInvocationType() + " " );
                    methodName = method.getLiteral();
                    code.append("aload " + varTable.get(object.getName()).getVirtualReg() + "\n");
                    invokeInstruction.append(jasminType(callInstruction.getFirstArg().getType(), imports));
                }

                invokeInstruction.append(methodName + '(');

                for (Element operand: callInstruction.getListOfOperands()) {
                    if (operand instanceof LiteralElement) {
                        LiteralElement constant = ((LiteralElement) operand);
                        code.append("iconst " + constant.getLiteral() + '\n');
                    }
                    else {
                        Operand op = (Operand) operand;
                        prefix = "i";
                        if (op.getType().getTypeOfElement() == ElementType.OBJECTREF)
                            prefix = "a";
                        code.append(prefix + "load " + varTable.get(op.getName()).getVirtualReg() + '\n');
                    }

                    invokeInstruction.append(jasminType(operand.getType(), imports));
                }

                invokeInstruction.append(")" + jasminType(callInstruction.getReturnType(), imports) + '\n');
                String invokeCode;
                if (callInstruction.getInvocationType() != CallType.invokevirtual) {
                    invokeCode = invokeInstruction.toString();
                    System.out.println(invokeCode);
                    code.append(invokeCode);
                }

                return code.toString();

            case GETFIELD:

                GetFieldInstruction getFieldInstruction   = (GetFieldInstruction) instruction;
                object                                    = (Operand) getFieldInstruction.getFirstOperand();
                field                                     = (Operand) getFieldInstruction.getSecondOperand();

                code.append("aload "    +  varTable.get(object.getName()).getVirtualReg() + "\n");
                code.append("getfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');
                return code.toString();

            case PUTFIELD:

                PutFieldInstruction putFieldInstruction   = (PutFieldInstruction) instruction;
                object                                    = (Operand) putFieldInstruction.getFirstOperand();
                field                                     = (Operand) putFieldInstruction.getSecondOperand();
                LiteralElement newValue                   = (LiteralElement) putFieldInstruction.getThirdOperand();

                code.append("aload " +  varTable.get(object.getName()).getVirtualReg() + "\n");
                code.append("bipush " +    newValue.getLiteral() + "\n");
                code.append("putfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');

                return code.toString();

        }
        return "";
    }


    public static String jasminType(Type fieldType, ArrayList<String> imports) {


        switch (fieldType.getTypeOfElement()) {
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
                return jasminType(fieldType);
        }
    }

    public static String jasminType(Type fieldType) {

        switch (fieldType.getTypeOfElement()) {
            case BOOLEAN:
                return "Z";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            case INT32:
                return "I";
            case OBJECTREF:
                return "a";
            default:
                return "";
        }
    }
}