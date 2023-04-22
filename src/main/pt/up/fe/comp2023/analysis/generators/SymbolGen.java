package pt.up.fe.comp2023.analysis.generators;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;


public class SymbolGen extends AJmmVisitor<Void,Void> {
    String name;
    Type type;

    @Override
    protected void buildVisitor() {
        addVisit("VarTypeSpecification",this::handleTypeSpecification);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleTypeSpecification(JmmNode jmmNode, Void unused) {
        name = jmmNode.get("varName");
        TypeGen typeGen = new TypeGen();
        typeGen.visit(jmmNode.getChildren().get(0));
        type = typeGen.getType();
        return null;
    }
    public Symbol getSymbol(){
        return new Symbol(type,name);
    }
}
