package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.specs.comp.ollir.ElementType.OBJECTREF;


public class JasminUtils {

    public static int stackLimit = 0;
    private static int tempLimit = 0;
    private static boolean hasAssign = false;
    public static String addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code = new StringBuilder();
        Operand object;
        Operand field;
        String prefix;
        String constType;

        switch(instruction.getInstType()){

            case ASSIGN :



                AssignInstruction assignInstruction = (AssignInstruction) instruction;
                Operand op1                         = (Operand) assignInstruction.getDest();
                Type type                           = (Type) op1.getType();
                prefix                              = "i";
                boolean ret                         = true;

                if (op1 instanceof ArrayOperand) {
                    ArrayOperand op = (ArrayOperand) op1;
                    Operand indexOperand = (Operand) op.getIndexOperands().get(0);
                    op1 = indexOperand;
                    prefix = "ia";

                    code.append("aload " + varTable.get(op.getName()).getVirtualReg() + "\n");
                    code.append("iload " + varTable.get(indexOperand.getName()).getVirtualReg() + "\n");
                    updateLimit(2);
                    updateLimit(-1);
                    ret = false;


                }


                if (type.getTypeOfElement() == ElementType.OBJECTREF || type.getTypeOfElement() == ElementType.ARRAYREF)
                    prefix = "a";
                hasAssign = true;
                code.append(addInstruction(assignInstruction.getRhs(), varTable, imports));
                hasAssign = false;

                code.append(prefix + "store ");
                if (ret)
                    code.append(varTable.get(op1.getName()).getVirtualReg());
                code.append("\n");

                updateLimit(-1);
                return code.toString();

            case NOPER:
                SingleOpInstruction singleOpInstruction = (SingleOpInstruction) instruction;
                Element onlyOperand                      = singleOpInstruction.getSingleOperand();

                if (onlyOperand instanceof LiteralElement) {
                    LiteralElement op = (LiteralElement) onlyOperand;
                    constType = constantPusher(op);
                    code.append(constType + op.getLiteral() + '\n');
                }
                else if (onlyOperand instanceof ArrayOperand) {

                    ArrayOperand op = (ArrayOperand) onlyOperand;
                    code.append("aload " + varTable.get(op.getName()).getVirtualReg() + "\n");
                    Operand indexOperand = (Operand) op.getIndexOperands().get(0);
                    if (op.getIndexOperands().get(0) instanceof Operand) {
                        code.append("iload " + varTable.get(indexOperand.getName()).getVirtualReg() + "\n");
                        code.append("iaload\n");
                        updateLimit(3);
                    }
                }
                else {
                        Operand op = (Operand) onlyOperand;
                        prefix = "i";
                        if (op.getType().getTypeOfElement() == ElementType.OBJECTREF || op.getType().getTypeOfElement() == ElementType.ARRAYREF)
                            prefix = "a";
                        code.append(prefix + "load " + varTable.get(op.getName()).getVirtualReg() + '\n');
                        updateLimit(1);
                }

                return code.toString();
            case BINARYOPER:

                BinaryOpInstruction binaryInstruction = (BinaryOpInstruction) instruction;
                Element left                          =  binaryInstruction.getLeftOperand();
                Element right                         =  binaryInstruction.getRightOperand();
                OperationType opType                  = binaryInstruction.getOperation().getOpType();

                if (right.isLiteral() && left.isLiteral()) {

                    LiteralElement l = (LiteralElement) left;
                    LiteralElement r = (LiteralElement) right;


                    switch (opType) {

                        case ADD:
                            code.append(constantPusher(Integer.parseInt(l.getLiteral()) + Integer.parseInt(r.getLiteral())));
                            code.append(Integer.parseInt(l.getLiteral()) + Integer.parseInt(r.getLiteral()) + "\n"); break;

                        case SUB:
                            code.append(constantPusher(Integer.parseInt(l.getLiteral()) - Integer.parseInt(r.getLiteral())));
                            code.append(Integer.parseInt(l.getLiteral()) - Integer.parseInt(r.getLiteral()) + "\n"); break;

                        case MUL:
                            code.append(constantPusher(Integer.parseInt(l.getLiteral()) * Integer.parseInt(r.getLiteral())));
                            code.append(Integer.parseInt(l.getLiteral()) * Integer.parseInt(r.getLiteral()) + "\n"); break;
                        case DIV:
                            code.append(constantPusher(Integer.parseInt(l.getLiteral()) / Integer.parseInt(r.getLiteral())));
                            code.append(Integer.parseInt(l.getLiteral()) / Integer.parseInt(r.getLiteral()) + "\n"); break;
                        default:
                            code.append("\n");
                    }

                    return code.toString();
                }

                addCodeOperand(varTable, code, left);
                addCodeOperand(varTable, code, right);
                updateLimit(-1);

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

                if (callInstruction.getInvocationType() == CallType.ldc)
                    return "ldc " + ((LiteralElement) callInstruction.getFirstArg()).getLiteral() + "\n";

                object                          = (Operand) callInstruction.getFirstArg();
                LiteralElement method           = (LiteralElement) callInstruction.getSecondArg();
                boolean addComma;
                String methodName;


                if (callInstruction.getInvocationType() == CallType.NEW) {

                    if (object.getName() == "array") {
                        for (Element element : callInstruction.getListOfOperands()) {
                            if (element instanceof Operand) {
                                Operand operand = (Operand) element;
                                updateLimit(1);

                                code.append("iload " + varTable.get(operand.getName()).getVirtualReg() + "\n");
                            }
                        }

                        code.append("newarray int\n");

                    }
                    else

                    code.append("new " +  jasminType(callInstruction.getFirstArg().getType(), imports) + "\n");
                    if (object.getType().getTypeOfElement() == OBJECTREF)
                        updateLimit(1);
                    return code.toString();
                }
                else if (callInstruction.getInvocationType() == CallType.invokestatic) {

                    methodName = method.getLiteral().replace("\"","");
                    invokeInstruction.append("invokestatic " + object.getName() + "/");
                }

                else if (callInstruction.getInvocationType() == CallType.arraylength) {

                    code.append("aload " + varTable.get(object.getName()).getVirtualReg() + "\n");
                    invokeInstruction.append(callInstruction.getInvocationType() + "\n");
                    updateLimit(1);
                    code.append(invokeInstruction);
                    return code.toString();
                }
                else {
                    invokeInstruction.append(callInstruction.getInvocationType() + " " );
                    methodName = method.getLiteral().replace("\"","");
                    code.append("aload " + varTable.get(object.getName()).getVirtualReg() + "\n");
                    updateLimit(1);
                    invokeInstruction.append(jasminType(callInstruction.getFirstArg().getType(), imports) + "/");

                }

                invokeInstruction.append("" + methodName + "(");

                for (Element operand: callInstruction.getListOfOperands()) {
                    if (operand instanceof LiteralElement) {
                        LiteralElement constant = ((LiteralElement) operand);

                        constType = constantPusher(constant);
                        code.append(constType + constant.getLiteral() + '\n');
                    }
                    else {
                        Operand op = (Operand) operand;
                        prefix = "i";
                        if (op.getType().getTypeOfElement() == ElementType.OBJECTREF || op.getType().getTypeOfElement() == ElementType.ARRAYREF)
                            prefix = "a";
                        code.append(prefix + "load " + varTable.get(op.getName()).getVirtualReg() + '\n');
                        updateLimit(1);
                    }

                    addComma = false;
                    if (operand.getType().getTypeOfElement() == OBJECTREF) {
                        invokeInstruction.append("L");
                        addComma = true;
                    }
                    invokeInstruction.append(jasminType(operand.getType(), imports));
                    if (addComma)
                        invokeInstruction.append(";");
                }

                invokeInstruction.append(")" + jasminType(callInstruction.getReturnType(), imports) + '\n');
                if (callInstruction.getInvocationType() != CallType.invokevirtual || methodName != "<init>") {
                    code.append(invokeInstruction);
                }

                if (!hasAssign && callInstruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
                    updateLimit(-1);
                    if (!(tempLimit < 0))
                        code.append("pop\n");
                }
                return code.toString();

            case GETFIELD:

                GetFieldInstruction getFieldInstruction   = (GetFieldInstruction) instruction;
                object                                    = (Operand) getFieldInstruction.getFirstOperand();
                field                                     = (Operand) getFieldInstruction.getSecondOperand();

                updateLimit(1);
                code.append("aload "    +  varTable.get(object.getName()).getVirtualReg() + "\n");
                code.append("getfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');

                return code.toString();

            case PUTFIELD:

                PutFieldInstruction putFieldInstruction   = (PutFieldInstruction) instruction;
                object                                    = (Operand) putFieldInstruction.getFirstOperand();
                field                                     = (Operand) putFieldInstruction.getSecondOperand();
                LiteralElement newValue                   = (LiteralElement) putFieldInstruction.getThirdOperand();

                updateLimit(1);
                code.append("aload " +  varTable.get(object.getName()).getVirtualReg() + "\n");

                constType = constantPusher(newValue);
                code.append(constType +  newValue.getLiteral() + "\n");
                code.append("putfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');

                return code.toString();

            case RETURN:

                ReturnInstruction returnInstruction = (ReturnInstruction) instruction;
                if (returnInstruction.getReturnType().getTypeOfElement() == ElementType.VOID) {
                    return "return\n";
                }

                else {

                    Element operand = returnInstruction.getOperand();
                    if (operand instanceof LiteralElement) {

                        LiteralElement constant = ((LiteralElement) operand);
                        constType = constantPusher(constant);
                        code.append(constType + constant.getLiteral() + '\n');

                        try {
                            Integer.parseInt(constant.getLiteral());
                        } catch (NumberFormatException e) {
                            code.append("areturn\n");
                            return code.toString();

                        }

                        code.append("ireturn\n");

                    } else {

                        Operand op = (Operand) operand;
                        prefix = "i";
                        if (op.getType().getTypeOfElement() == ElementType.OBJECTREF || op.getType().getTypeOfElement() == ElementType.ARRAYREF)
                            prefix = "a";
                        code.append(prefix + "load " + varTable.get(op.getName()).getVirtualReg() + '\n');
                        updateLimit(1);
                        code.append(prefix + "return\n");

                    }
                }

                return code.toString();


        }
        return "";
    }

    private static void addCodeOperand(HashMap<String, Descriptor> varTable, StringBuilder code, Element element) {
        if (!element.isLiteral()) {
            Operand el = (Operand) element;
            if (element.getType().getTypeOfElement() != ElementType.BOOLEAN || element.getType().getTypeOfElement() != ElementType.INT32)
                code.append("iload ");
            else
                code.append("aload ");
            code.append(varTable.get(el.getName()).getVirtualReg() + "\n");
            updateLimit(1);
        }
        else {
            LiteralElement el = (LiteralElement) element;
            code.append(constantPusher(el) + el.getLiteral() + "\n");
        }
    }


    public static String jasminType(Type fieldType, ArrayList<String> imports) {


        switch (fieldType.getTypeOfElement()) {
            case THIS:
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
                        if (fieldType instanceof ArrayType) {
                            return "[".repeat(dimensions) + 'L' + statement.replace("\\.", "/") + ';';
                        }
                        else
                            return statement.replace("\\.", "/");
                    }
                }

                if (fieldType instanceof ArrayType) {
                    boolean reference = false;
                        Type newFieldType = new Type(((ArrayType) fieldType).getElementType().getTypeOfElement());
                    if (jasminType(newFieldType, imports) != "I" && jasminType(newFieldType) != "Z")
                        reference = true;

                    return "[".repeat(dimensions) + (reference ? "L" : "") + jasminType(newFieldType, imports) + (reference ? ";" : "");
                }

            default:
                return jasminType(fieldType);
        }
    }

    public static void updateLimit(Integer value) {

        tempLimit += value;
        stackLimit = Math.max(tempLimit, stackLimit);

    }

    public static void resetLimit() {
        tempLimit  = 0;
        stackLimit = 0;
    }

    public static String constantPusher (LiteralElement op) {

        updateLimit(1);

        int num;
        try {
            num = Integer.parseInt(op.getLiteral());
        } catch (NumberFormatException e) {
            return "ldc ";

        }

        if (-1 < num && num <= 5) return "iconst_";
        else if (-127 < num && num < 128) return "bipush ";
        else if (-32768 < num && num < 32767) return "sipush ";
        else return "ldc ";

    }

    public static String constantPusher (int num) {

        updateLimit(1);

        if (-1 < num && num <= 5) return "iconst_";
        else if (-127 < num && num < 128) return "bipush ";
        else if (-32768 < num && num < 32767) return "sipush ";
        else return "ldc ";

    }
    public static String jasminType(Type fieldType) {

        switch (fieldType.getTypeOfElement()) {
            case BOOLEAN:
                return "Z";
            case STRING:
                return "java/lang/String";
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