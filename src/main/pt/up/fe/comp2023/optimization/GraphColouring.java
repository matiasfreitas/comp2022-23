package pt.up.fe.comp2023.optimization;

import pt.up.fe.comp.jmm.ast.JmmNode;
import org.specs.comp.ollir.*;

import java.util.*;

public class GraphColouring {
    HashMap<Integer, GraphVertice> vertices;
    int mRegisters;
    boolean isStatic;
    HashMap<String, Descriptor> table;

    boolean kIsZeroFlag;

    HashMap<String, Integer> registers = new HashMap<>();

    public GraphColouring(ArrayList<HashMap<Node, BitSet>> ranges, Method method, boolean kIsZeroFlag) {
        this.kIsZeroFlag = kIsZeroFlag;
        vertices = new HashMap<>();
        table = method.getVarTable();
        isStatic = method.isStaticMethod();

        if(isStatic) mRegisters = 0;
        else mRegisters = 1;

        for(String name: table.keySet()) {

            Descriptor descriptor = table.get(name);
            vertices.put(descriptor.getVirtualReg(), new GraphVertice(name, descriptor.getVirtualReg()));
        }

        for (HashMap<Node, BitSet> range: ranges) {
            for (Node node : range.keySet()) {

                BitSet bitset = range.get(node);
                List<Integer> indexes = new ArrayList<>();

                for (int i = 0; i < bitset.length(); i++) {

                    if (bitset.get(i)) indexes.add(i);
                }


                for (int i = 0; i < indexes.size() - 1; i++) {

                    GraphVertice verticeEdit = vertices.get(indexes.get(i));
                    for (int j = i + 1; j < indexes.size(); j++) {

                        GraphVertice vertice2 = vertices.get(indexes.get(j));
                        verticeEdit.addPath(new GraphPath(verticeEdit, vertice2));
                        vertice2.addPath(new GraphPath(vertice2, verticeEdit));
                    }
                }
            }
        }
    }

    public boolean KColoring(int k) {
        if (k < mRegisters) {

            System.out.println( Integer.toString(k) +" registers isn't enough.");
            return false;
        }

        Stack<GraphVertice> verticeStack = new Stack<>();

        while (!vertices.isEmpty()) {

            Iterator<Map.Entry<Integer, GraphVertice>> iterator = vertices.entrySet().iterator();
            while (iterator.hasNext()) {

                GraphVertice node = iterator.next().getValue();
                if (node.getNPaths() < k) {

                    verticeStack.push(node);
                    node.setActive(false);
                    iterator.remove();
                }
            }
        }

        HashMap<Integer, ArrayList<String>> colors = new HashMap<>();

        for (int i = mRegisters; i < k; i++) colors.put(i, new ArrayList<>());
        HashMap<String, Descriptor> newTable = new HashMap<>();
        while (!verticeStack.isEmpty()) {

            GraphVertice vertice = verticeStack.pop();
            vertice.setActive(true);
            vertices.put(vertice.getReg(), vertice);
            boolean colored = false;
            for (Integer registers : colors.keySet()) {

                boolean canColor = true;
                for (String var : vertice.getLinkedVertices()) {

                    if (colors.get(registers).contains(var))

                        canColor = false;
                }

                if (canColor) {

                    colors.get(registers).add(vertice.getName());
                    Descriptor oldDescriptor = table.get(vertice.getName());
                    newTable.put(vertice.getName(), new Descriptor(oldDescriptor.getScope(), registers, oldDescriptor.getVarType()));
                    colored = true;

                    break;
                }
            }

            if (!colored) {

                System.out.println(k + " is insufficient registers to this method");

                while (!verticeStack.isEmpty()) {

                    GraphVertice vertice1 = verticeStack.pop();
                    vertice1.setActive(true);
                    vertices.put(vertice1.getReg(), vertice1);
                }
                return false;
            }
        }

        int register;
        if(isStatic) register = 0;
        else register = 1;

        for (String name: table.keySet()) {

            Descriptor descriptor = table.get(name);
            if (descriptor.getScope() == VarScope.PARAMETER || descriptor.getScope() == VarScope.FIELD) {

                newTable.put(name, new Descriptor(descriptor.getScope(), register, descriptor.getVarType()));
                registers.put(name, register);
                register++;

            }

        }

        ArrayList<Integer> usedRegisters = new ArrayList<>();

        for (Descriptor descriptor: newTable.values()) {
            if(!usedRegisters.contains(descriptor.getVirtualReg()))
                usedRegisters.add(descriptor.getVirtualReg());
        }
        if (!usedRegisters.contains(0))
            usedRegisters.add(0);

        for (String key:newTable.keySet()) {
            table.replace(key, newTable.get(key));
        }
        System.out.println("Allocated " + usedRegisters.size() + " registers");
        return true;
    }


    public HashMap<String, Integer> getRegisters() {
        return registers;
    }

    public int getmRegisters() {
        return mRegisters;
    }
}
