package pt.up.fe.comp2023.optimization;

import org.specs.comp.ollir.*;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;

public class Liveness {
    public ArrayList<HashMap<Node, BitSet>> liveness(Method method) {

        HashMap<Node, BitSet> definition = new HashMap<>();
        HashMap<Node, BitSet> using = new HashMap<>();
        HashMap<Node, BitSet> input = new HashMap<>();
        HashMap<Node, BitSet> output = new HashMap<>();

        int nVariables = method.getVarTable().size();

        for (Instruction instruction : method.getInstructions()) {

            definition.put(instruction, getDefinedVars(instruction, method.getVarTable()));
            using.put(instruction, getUsedVars(instruction, method.getVarTable()));
            input.put(instruction, new BitSet(nVariables));
            output.put(instruction, new BitSet(nVariables));
        }

        boolean made = false;
        int i = 0;

        ArrayList<Instruction> instructionsNodes = new ArrayList<>(method.getInstructions());
        Collections.reverse(instructionsNodes);

        while (!made) {

            System.out.println("Trying: " + i);
            i++;
            HashMap<Node, BitSet> in_temp = new HashMap<>(input);
            HashMap<Node, BitSet> out_temp = new HashMap<>(output);

            for (Instruction instruction : instructionsNodes) {

                BitSet newOutput = new BitSet(nVariables);
                if (instruction.getSucc1() != null) {

                    if (instruction.getSucc1().getNodeType() != NodeType.END) {

                        newOutput = (BitSet) input.get(instruction.getSucc1()).clone();
                        if (instruction.getSucc2() != null) {

                            newOutput.or(input.get(instruction.getSucc2()));
                        }
                    }
                }

                output.replace(instruction, newOutput);
                BitSet newInput = (BitSet) output.get(instruction).clone();
                BitSet tempDefinition = definition.get(instruction);

                for (int index = 0; index < nVariables; index++) {

                    if (newInput.get(index) && !tempDefinition.get(index)) newInput.set(index);
                    else newInput.clear(index);
                }

                newInput.or(using.get(instruction));
                input.replace(instruction, newInput);
            }


            made = true;
            for (Instruction instruction : instructionsNodes) {

                if (!input.get(instruction).equals(in_temp.get(instruction)))

                    made = false;
                if (!output.get(instruction).equals(out_temp.get(instruction)))

                    made = false;
            }
        }

        ArrayList<HashMap<Node, BitSet>> result = new ArrayList<>();
        result.add(input);
        result.add(output);

        return result;
    }

    private BitSet getDefinedVars(Instruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();

        if (instruction.getInstType() == InstructionType.ASSIGN)
            setElementBit(variable, ((AssignInstruction) instruction).getDest(), table);

        return variable;
    }

    private BitSet getUsedVars(Instruction instruction, HashMap<String, Descriptor> table) {

        switch (instruction.getInstType()) {
            case CALL:

                return getUsedVarsCall((CallInstruction) instruction, table);
            case NOPER:

                return getUsedVariablesToSingleOp((SingleOpInstruction) instruction, table);
            case ASSIGN:

                return getUsedVariablesToAssign((AssignInstruction) instruction, table);

            case RETURN:

                return getUsedVariablesReturn((ReturnInstruction) instruction, table);
            case GETFIELD:

                return getUsedVariablesGetField((GetFieldInstruction) instruction, table);
            case PUTFIELD:

                return getUsedVariablesPutField((PutFieldInstruction) instruction, table);

            case BINARYOPER:

                return getUsedVariablesBinaryOp((BinaryOpInstruction) instruction, table);
            default:

                break;
        }
        return new BitSet();
    }

    private BitSet getUsedVarsCall(CallInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        setElementBit(variable, instruction.getFirstArg(), table);

        if (instruction.getNumOperands() > 1) {

            if (instruction.getInvocationType() != CallType.NEW)
                setElementBit(variable, instruction.getSecondArg(), table);

            for (Element arg : instruction.getListOfOperands())
                setElementBit(variable, arg, table);
        }

        return variable;
    }

    private BitSet getUsedVariablesToSingleOp(SingleOpInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        setElementBit(variable, instruction.getSingleOperand(), table);

        return variable;
    }

    private BitSet getUsedVariablesToAssign(AssignInstruction instruction, HashMap<String, Descriptor> table) {

        return getUsedVars(instruction.getRhs(), table);
    }

    private BitSet getUsedVariablesReturn(ReturnInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        if (instruction.hasReturnValue())
            setElementBit(variable, instruction.getOperand(), table);

        return variable;
    }

    private BitSet getUsedVariablesGetField(GetFieldInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        setElementBit(variable, instruction.getFirstOperand(), table);
        setElementBit(variable, instruction.getSecondOperand(), table);

        return variable;
    }

    private BitSet getUsedVariablesPutField(PutFieldInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        setElementBit(variable, instruction.getFirstOperand(), table);
        setElementBit(variable, instruction.getSecondOperand(), table);
        setElementBit(variable, instruction.getThirdOperand(), table);

        return variable;
    }

    private BitSet getUsedVariablesBinaryOp(BinaryOpInstruction instruction, HashMap<String, Descriptor> table) {

        BitSet variable = new BitSet();
        setElementBit(variable, instruction.getRightOperand(), table);
        setElementBit(variable, instruction.getLeftOperand(), table);

        return variable;
    }

    private void setElementBit(BitSet variable, Element element, HashMap<String, Descriptor> table) {

        if (element.getType().getTypeOfElement() == ElementType.THIS) {
            variable.set(0);
            return;
        }

        if (element.isLiteral())
            return;

        Descriptor descriptor = table.get(((Operand) element).getName());

        if (descriptor.getVarType().getTypeOfElement() == ElementType.ARRAYREF
                && element.getType().getTypeOfElement() == ElementType.INT32) {
            for (Element index : ((ArrayOperand) element).getIndexOperands())
                setElementBit(variable, index, table);
        }

        if (descriptor.getScope() == VarScope.PARAMETER || descriptor.getScope() == VarScope.FIELD)
            return;

        int register = descriptor.getVirtualReg();

        variable.set(register);
    }
}
