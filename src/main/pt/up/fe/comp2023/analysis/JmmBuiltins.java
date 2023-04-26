package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class JmmBuiltins {

    // TODO add JmmVoid builtin?

    public static Type JmmVoid = new Type("void",false);
    public static Type JmmInt = new Type("int",false);
    public static Type JmmChar = new Type("char",false);
    public static Type JmmBoolean = new Type("boolean",false);

    public static Type JmmString = new Type("String",false);

    public static Type JmmAssumeType = new Type("JmmBuiltinAssumeType",false);

    public static List<Type> builtinTypes(){
        return Arrays.asList(JmmInt,JmmChar,JmmString,JmmBoolean,JmmVoid);
    }

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
    public static boolean typeEqualOrAssumed(Type left,Type right){
        boolean assumed = left.equals(JmmAssumeType) || right.equals(JmmAssumeType);
        boolean equal = left.equals(right);
        return assumed || equal;
    }
    public static boolean typesEqualOrAssumed(List<Type> types, Type right){
        boolean result = true;
        for(Type t: types){
            result = result && typeEqualOrAssumed(t,right);
        }
        return result;
    }
}
