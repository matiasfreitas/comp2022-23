package pt.up.fe.comp2023;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Objects;

public class TypeGen extends AJmmVisitor<Void,Void> {
    Integer dimensions = 0;
    String typeName = "";
    Boolean builtIn = false;
    @Override
    protected void buildVisitor() {
        addVisit("ArrayType",this::handleArrayType);
        addVisit("ObjectType",this::handleObjectType);
        addVisit("BuiltInType",this::handleBuiltinType);
        this.setDefaultVisit(this::visitAllChildren);
    }

    private Void handleObjectType(JmmNode jmmNode, Void unused)
    {
        this.builtIn = false;
        this.typeName = jmmNode.get("typeName");
        return  null;
    }

    private Void handleBuiltinType(JmmNode jmmNode, Void unused) {
        this.builtIn =true;
        this.typeName = jmmNode.get("typeName");
        return  null;
    }

    private Void handleArrayType(JmmNode jmmNode, Void unused) {
        this.dimensions++;
        this.visitAllChildren(jmmNode,unused);
        return null;
    }

    public  Type getType(){
        return  new Type(typeName,dimensions >0);

    }


}
