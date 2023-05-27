package pt.up.fe.comp2023.jasmin;

import com.google.gson.annotations.Since;
import org.specs.comp.ollir.*;

import javax.sound.midi.SysexMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.specs.comp.ollir.ElementType.*;


public class JasminUtils {

    public static int stackLimit  = 0;
    private static int tempLimit  = 0;
    public static int labelNumber = 0;
    private static boolean hasAssign  = false;
    private static boolean iincAssign = false;

    private static String createAssignCode(AssignInstruction assignInstruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code = new StringBuilder();
        Operand op1        = (Operand) assignInstruction.getDest();
        Type type          = (Type) op1.getType();
        String prefix      = "i";

        if (op1 instanceof ArrayOperand) {

            ArrayOperand op      = (ArrayOperand) op1;
            Operand indexOperand = (Operand) op.getIndexOperands().get(0);

            code.append("aload" + getRegisterHandle(varTable.get(op.getName()).getVirtualReg()) + varTable.get(op.getName()).getVirtualReg() + "\n");
            code.append("iload" + getRegisterHandle(varTable.get(indexOperand.getName()).getVirtualReg()) + varTable.get(indexOperand.getName()).getVirtualReg() + "\n");
            updateLimit(2);
            hasAssign = true;
            code.append(addInstruction(assignInstruction.getRhs(), varTable, imports));
            code.append("iastore\n");
            hasAssign = false;
            updateLimit(-1);
            return code.toString();


        }

        else if (type.getTypeOfElement() == ElementType.OBJECTREF || type.getTypeOfElement() == ElementType.ARRAYREF)
            prefix = "a";

        hasAssign  = true;
        iincAssign = false;

        String iincCode = "";
        if (assignInstruction.getRhs().getInstType() == InstructionType.BINARYOPER ) {
            iincCode = generateIincCode(assignInstruction, varTable, imports);
            if (iincAssign) {
                return iincCode;
            }
        }

        code.append(addInstruction(assignInstruction.getRhs(), varTable, imports));
        code.append(prefix + "store");
        code.append(getRegisterHandle(varTable.get(op1.getName()).getVirtualReg()) + varTable.get(op1.getName()).getVirtualReg());
        code.append("\n");
        updateLimit(-1);

        hasAssign  = false;
        return code.toString();
    }

    public static String createUnaryCode(UnaryOpInstruction unaryOpInstruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code     = new StringBuilder();
        Element unaryOperand   = unaryOpInstruction.getOperand();
        Operation operation    = unaryOpInstruction.getOperation();
        code.append(loadVariable(unaryOperand, varTable));

        switch(operation.getOpType()){
            case NOT:
            case NOTB: code.append(boolJumpOperation("ifeq"));
        }

        return code.toString();

    }

    public static String createBinaryCode(BinaryOpInstruction binaryInstruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code     =  new StringBuilder();
        Element left           =  binaryInstruction.getLeftOperand();
        Element right          =  binaryInstruction.getRightOperand();
        OperationType opType   =  binaryInstruction.getOperation().getOpType();


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
                    if(boolLiteralOperation(opType, l, r)) {
                        code.append(constantPusher(1) +  "1\n");
                    }
                    else code.append(constantPusher(0) + "0\n");
                    break;

            }

            return code.toString();
        }

        boolean leftIsZero  = false;
        boolean rightIsZero = false;

        if (left.isLiteral()) {
            int value = Integer.parseInt(((LiteralElement) left).getLiteral());
            if (value == 0) {
                leftIsZero = true;
            }
        }

        if (right.isLiteral()) {
            int value = Integer.parseInt(((LiteralElement) right).getLiteral());
            if (value == 0) {
                rightIsZero = true;
            }
        }

        if(!leftIsZero  || isArithmetic(opType)) addCodeOperand(varTable, code, left);
        if(!rightIsZero || isArithmetic(opType)) addCodeOperand(varTable, code, right);

        updateLimit(-1);


        switch (opType) {

            case ADD: code.append("iadd\n"); break;
            case SUB: code.append("isub\n"); break;
            case MUL: code.append("imul\n"); break;
            case DIV: code.append("idiv\n"); break;
            case AND, ANDB: code.append("iand\n"); break;
            case LTE, LTH:

                if (leftIsZero) code.append(boolJumpOperation("ifgt"));
                else if (rightIsZero) code.append(boolJumpOperation("iflt"));
                else code.append(boolJumpOperation("if_icmplt"));
                break;

            case GTE, GTH:

                if (leftIsZero) code.append(boolJumpOperation("iflt"));
                else if (rightIsZero) code.append(boolJumpOperation("ifgt"));
                else code.append(boolJumpOperation("if_icmpgt"));
                break;

            case EQ: code.append(boolJumpOperation("ifne"));break;
            case NEQ: code.append(boolJumpOperation("ifqe"));break;

            default:
                code.append("\n");
        }
        return code.toString();

    }

    public static String createBranchCode(CondBranchInstruction conditionInstruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code      = new StringBuilder();
        Instruction condition = conditionInstruction.getCondition();
        code.append(addInstruction(condition, varTable, imports));

        code.append("ifne ");
        code.append(conditionInstruction.getLabel() + "\n");

        return code.toString();
    }

    public static String createCallCode(CallInstruction callInstruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {
        StringBuilder code              = new StringBuilder();
        StringBuilder invokeInstruction = new StringBuilder();

        if (callInstruction.getInvocationType() == CallType.ldc)
            return "ldc " + ((LiteralElement) callInstruction.getFirstArg()).getLiteral() + "\n";

        Operand object                          = (Operand) callInstruction.getFirstArg();
        LiteralElement method           = (LiteralElement) callInstruction.getSecondArg();
        boolean addComma;
        String methodName;


        if (callInstruction.getInvocationType() == CallType.NEW) {

            if (object.getName() == "array") {
                for (Element element : callInstruction.getListOfOperands()) {
                    if (element instanceof Operand) {
                        Operand operand = (Operand) element;
                        updateLimit(1);

                        code.append("iload" + getRegisterHandle(varTable.get(operand.getName()).getVirtualReg()) + varTable.get(operand.getName()).getVirtualReg() + "\n");
                    }
                }

                code.append("newarray int\n");

            }
            else
                code.append("new " +  jasminType(callInstruction.getFirstArg().getType(), imports) + "\ndup\n");
            if (object.getType().getTypeOfElement() == OBJECTREF)
                updateLimit(1);
            return code.toString();
        }
        else if (callInstruction.getInvocationType() == CallType.invokestatic) {

            methodName = method.getLiteral().replace("\"","");
            invokeInstruction.append("invokestatic " + object.getName() + "/");
        }

        else if (callInstruction.getInvocationType() == CallType.arraylength) {

            code.append("aload" + getRegisterHandle(varTable.get(object.getName()).getVirtualReg()) + varTable.get(object.getName()).getVirtualReg() + "\n");
            invokeInstruction.append(callInstruction.getInvocationType() + "\n");
            updateLimit(1);
            code.append(invokeInstruction);
            return code.toString();
        }

        else {

            invokeInstruction.append(callInstruction.getInvocationType() + " " );
            methodName = method.getLiteral().replace("\"","");
            code.append("aload" + getRegisterHandle(varTable.get(object.getName()).getVirtualReg()) + varTable.get(object.getName()).getVirtualReg() + "\n");
            updateLimit(1);
            invokeInstruction.append(jasminType(callInstruction.getFirstArg().getType(), imports) + "/");

        }

        invokeInstruction.append("" + methodName + "(");

        for (Element operand: callInstruction.getListOfOperands()) {
            code.append(loadVariable(operand, varTable));
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
            code.append("pop\n");
        }
        return code.toString();
    }

    public static String createGetFieldCode(GetFieldInstruction getFieldInstruction,  HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code  = new StringBuilder();
        Operand object      = (Operand) getFieldInstruction.getFirstOperand();
        Operand field       = (Operand) getFieldInstruction.getSecondOperand();

        updateLimit(1);
        code.append("aload" +  varTable.get(object.getName()).getVirtualReg() + "\n");
        code.append("getfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');

        return code.toString();
    }

    public static String createPutFieldCode(PutFieldInstruction putFieldInstruction,  HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code = new StringBuilder();
        Operand object     = (Operand) putFieldInstruction.getFirstOperand();
        Operand field      = (Operand) putFieldInstruction.getSecondOperand();
        Element newValue   = putFieldInstruction.getThirdOperand();

        updateLimit(1);
        code.append("aload" + getRegisterHandle(varTable.get(object.getName()).getVirtualReg()) + varTable.get(object.getName()).getVirtualReg() + "\n");
        code.append(loadVariable(newValue, varTable));
        code.append("putfield Dummy/" + field.getName() + ' ' + jasminType(field.getType(), imports) + '\n');

        return code.toString();
    }

    public static String createReturnCode(ReturnInstruction returnInstruction,  HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        StringBuilder code = new StringBuilder();
        if (returnInstruction.getOperand() == null) return "return\n";
        code.append(loadVariable(returnInstruction.getOperand(), varTable));
        ElementType returnType = returnInstruction.getOperand().getType().getTypeOfElement();
        if (returnType == BOOLEAN || returnType == INT32) code.append("i");
        else if (returnType == ARRAYREF || returnType == OBJECTREF) code.append("a");
        code.append("return\n");
        return code.toString();
    }
    public static String addInstruction(Instruction instruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {

        switch(instruction.getInstType()){

            case ASSIGN :    return createAssignCode((AssignInstruction) instruction, varTable, imports);
            case UNARYOPER:  return createUnaryCode((UnaryOpInstruction) instruction, varTable, imports);
            case BINARYOPER: return createBinaryCode((BinaryOpInstruction) instruction, varTable, imports);
            case BRANCH:     return createBranchCode((CondBranchInstruction) instruction, varTable, imports);
            case CALL:       return createCallCode((CallInstruction) instruction, varTable, imports);
            case GETFIELD:   return createGetFieldCode((GetFieldInstruction) instruction, varTable, imports);
            case PUTFIELD:   return createPutFieldCode((PutFieldInstruction) instruction, varTable, imports);
            case RETURN:     return createReturnCode((ReturnInstruction) instruction, varTable, imports);
            case NOPER:      return loadVariable(((SingleOpInstruction) instruction).getSingleOperand(), varTable);
            case GOTO:       return "goto " + ((GotoInstruction) instruction).getLabel() + "\n";
            default:         return "";

        }
    }

    private static boolean isArithmetic(OperationType opType) {
        return opType == OperationType.ADD || opType == OperationType.SUB || opType == OperationType.MUL || opType == OperationType.DIV;
    }
    private static String loadVariable(Element operand, HashMap<String, Descriptor> varTable) {

        StringBuilder code = new StringBuilder();
        String prefix = "";
        if (operand instanceof LiteralElement) {
            LiteralElement op = (LiteralElement) operand;
            code.append(constantPusher(op) + op.getLiteral() + '\n');
        }
        else if (operand instanceof ArrayOperand) {

            ArrayOperand op = (ArrayOperand) operand;
            code.append("aload" + getRegisterHandle(varTable.get(op.getName()).getVirtualReg()) + varTable.get(op.getName()).getVirtualReg() + "\n");
            Operand indexOperand = (Operand) op.getIndexOperands().get(0);
            if (op.getIndexOperands().get(0) instanceof Operand) {
                code.append("iload" + getRegisterHandle(varTable.get(indexOperand.getName()).getVirtualReg()) + varTable.get(indexOperand.getName()).getVirtualReg() + "\n");
                code.append("iaload\n");
                updateLimit(3);
            }
        }
        else {
            Operand op = (Operand) operand;
            prefix = "i";
            if (op.getType().getTypeOfElement() == ElementType.OBJECTREF || op.getType().getTypeOfElement() == ElementType.ARRAYREF)
                prefix = "a";
            code.append(prefix + "load" + getRegisterHandle(varTable.get(op.getName()).getVirtualReg()) + varTable.get(op.getName()).getVirtualReg() + '\n');
            updateLimit(1);
        }

        return code.toString();

    }

    private static String generateIincCode(AssignInstruction instruction, HashMap<String, Descriptor> varTable, ArrayList<String> imports) {
        if (instruction.getRhs().getInstType() == InstructionType.BINARYOPER) {
            BinaryOpInstruction rightInstruction = (BinaryOpInstruction) instruction.getRhs();
            OperationType opType = rightInstruction.getOperation().getOpType();

            Element left  = rightInstruction.getLeftOperand();
            Element right = rightInstruction.getRightOperand();
            Operand assignee = (Operand) instruction.getDest();

            if (left instanceof Operand) {
                if(((Operand) left).getName().equals(assignee.getName())) {
                    if (right.isLiteral()) {
                        int num = Integer.parseInt(((LiteralElement) right).getLiteral());
                        if ((opType == OperationType.ADD && num >= 0 && num <= 127) || (opType == OperationType.SUB && num >= 0 && num <= 128)) {
                            iincAssign = true;
                            String signal = (opType == OperationType.ADD)? "": "-";
                            if (num == 0)
                                return "";
                            return "iinc " + varTable.get(assignee.getName()).getVirtualReg() + " " + signal + num + "\n";
                        }
                    }
                }
            }

            else if (right instanceof Operand) {
                if(((Operand) right).getName().equals(assignee.getName())) {
                    if (left.isLiteral()) {
                        int num = Integer.parseInt(((LiteralElement) left).getLiteral());
                        if ((opType == OperationType.ADD && num >= 0 && num <= 127) || (opType == OperationType.SUB && num >= 0 && num <= 128)) {
                            iincAssign = true;
                            String signal = (opType == OperationType.ADD)? "": "-";
                            if (num == 0)
                                return "";
                            return "iinc " + varTable.get(assignee.getName()).getVirtualReg() + " " + signal + num + "\n";
                        }
                    }
                }
            }
        }


        return "";

    }
    private static boolean boolLiteralOperation (OperationType opType, LiteralElement l, LiteralElement r) {
            boolean firstFlag  = (l.getLiteral().equals("1")) ? true : false;
            boolean secondFlag = (r.getLiteral().equals("1")) ? true : false;

            switch (opType) {

                case AND, ANDB: return firstFlag && secondFlag;
                case OR, ORB: return firstFlag || secondFlag;

                case EQ: return firstFlag == secondFlag;
                case NEQ: return firstFlag != secondFlag;
                case LTE, LTH: return Integer.parseInt(l.getLiteral()) < Integer.parseInt(r.getLiteral());
                case GTE, GTH: return Integer.parseInt(l.getLiteral()) > Integer.parseInt(r.getLiteral());
                default:
                    return false;
        }
    }
    private static String boolJumpOperation (String prefix) {

        StringBuilder code = new StringBuilder();
        code.append(prefix + " STEP" + labelNumber + "\n" );
        code.append("iconst_0\n");
        code.append("goto GO" + labelNumber +"\n");
        code.append("STEP" + labelNumber + ":\n");
        code.append("iconst_1\n");
        code.append("GO" + labelNumber + ":\n");
        updateLimit(1);
        labelNumber++;
        return code.toString();

    }
    private static void addCodeOperand(HashMap<String, Descriptor> varTable, StringBuilder code, Element element) {
        if (!element.isLiteral()) {
            Operand el = (Operand) element;
            if (element.getType().getTypeOfElement() != ElementType.BOOLEAN || element.getType().getTypeOfElement() != ElementType.INT32)
                code.append("iload" + getRegisterHandle(varTable.get(el.getName()).getVirtualReg()));
            else
                code.append("aload" + getRegisterHandle(varTable.get(el.getName()).getVirtualReg()));
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

        if (0 <= num && num < 6)          return "iconst_";
        else if (0 <= num && num < 128)   return "bipush ";
        else if (0 <= num && num < 32767) return "sipush ";
        else return "ldc ";

    }



    public static String constantPusher (int num) {

        updateLimit(1);

        if (0 <= num && num < 6)          return "iconst_";
        else if (0 <= num && num < 128)   return "bipush ";
        else if (0 <= num && num < 32767) return "sipush ";
        else return "ldc ";

    }
    public static String jasminType(Type fieldType) {

        switch (fieldType.getTypeOfElement()) {
            case BOOLEAN:   return "Z";
            case STRING:    return "java/lang/String";
            case VOID:      return "V";
            case INT32:     return "I";
            case OBJECTREF: return "a";
            default:
                return "";
        }
    }

    private static String getRegisterHandle(int num) {
        if (num < 4) return "_";
        return " ";
    }
}