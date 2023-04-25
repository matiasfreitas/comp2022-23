package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Optional;

public class JmmBuiltins {

    public static Type JmmInt = new Type("int",false);
    public static Type JmmChar = new Type("char",false);
    public static Type JmmBoolean = new Type("boolean",false);

    public static Type JmmString = new Type("String",false);

    public static Type JmmAssumeType = new Type("JmmBuiltinAssumeType",false);


    public static Optional<Type> fromJmmNode(JmmNode node){
        String kind = node.getKind();
        Type t = switch (kind) {
            case "Int" -> JmmInt;
            case "Char" -> JmmChar;
            case "Boolean" -> JmmBoolean;
            case "String" -> JmmString;
            default -> null;
        };
        return  Optional.ofNullable(t);
    }
}
