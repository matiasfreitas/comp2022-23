package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JmmBuiltins {

    // TODO add JmmVoid builtin?

    public static Type JmmVoid = new Type("void", false);
    public static Type JmmInt = new Type("int", false);
    public static Type JmmChar = new Type("char", false);
    public static Type JmmBoolean = new Type("boolean", false);

    public static Type JmmString = new Type("String", false);

    public static Type JmmAssumeType = new Type("JmmBuiltinAssumeType", false);

    public static List<Type> builtinTypes() {
        return Arrays.asList(JmmInt, JmmChar, JmmString, JmmBoolean, JmmVoid);
    }


    private static final List<String> literals = Arrays.asList("Int", "Boolean", "Char", "String");

    private static final List<String> substitutable = Arrays.asList("Identifier", "This");

    private static Optional<Type> kindToType(String kind) {
        Type t = switch (kind) {
            case "Int" -> JmmInt;
            case "Char" -> JmmChar;
            case "Boolean" -> JmmBoolean;
            case "String" -> JmmString;
            default -> null;
        };
        return Optional.ofNullable(t);
    }

    private static String typeToKind(Type t) {
        return switch (t.getName()) {
            case "int" -> "Int";
            case "boolean" -> "Boolean";
            default -> null;
        };
    }

    public static Optional<Type> fromJmmNode(JmmNode node) {
        String kind = node.getKind();
        return kindToType(kind);
    }

    public static Type fromAnnotatedNode(JmmNode node) {
        var isArray = node.get("isArray").equals("true");
        var type = node.get("type");
        if (isArray) {
            return new Type(type, true);
        }
        return switch (type) {
            case "int" -> JmmInt;
            case "char" -> JmmChar;
            case "boolean" -> JmmBoolean;
            case "String" -> JmmString;
            case "void" -> JmmVoid;
            case "JmmBuiltinAssumeType" -> JmmAssumeType;
            default -> new Type(type, false);
        };
    }

    public static boolean typeEqualOrAssumed(Type left, Type right) {
        boolean assumed = left.equals(JmmAssumeType) || right.equals(JmmAssumeType);
        boolean equal = left.equals(right);
        return assumed || equal;
    }

    public static boolean typesEqualOrAssumed(List<Type> types, Type right) {
        boolean result = true;
        for (Type t : types) {
            result = result && typeEqualOrAssumed(t, right);
        }
        return result;
    }

    public static void annotate(JmmNode jmmNode, Type t) {
        jmmNode.put("type", t.getName());
        jmmNode.put("isArray", (t.isArray()) ? "true" : "false");
    }

    public static boolean isLiteralNode(JmmNode jmmNode) {
        return literals.contains(jmmNode.getKind());
    }

    public static boolean noNeedParenthesis(JmmNode jmmNode) {
        return literals.contains(jmmNode.getKind()) || substitutable.contains(jmmNode.getKind());
    }


    public static JmmNode newIntNode(String value) {
        var node = new JmmNodeImpl("Int");
        node.put("value", value);
        return node;
    }

    public static JmmNode newBooleanNode(String value) {
        var node = new JmmNodeImpl("Boolean");
        node.put("value", value);
        return node;
    }

    public static JmmNode newNode(JmmNode jmmNode) {
        var node = new JmmNodeImpl(jmmNode.getKind());
        node.put("value", jmmNode.get("value"));
        return node;
    }

}
