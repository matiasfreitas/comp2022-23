package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.JmmBuiltins;

import java.util.Optional;


public record OllirSymbol(String value, String type) {

    public static OllirSymbol noSymbol() {
        return new OllirSymbol(null, null);
    }

    public static OllirSymbol fromLiteral(JmmNode literal) {
        String type = switch (literal.getKind()) {
            case "Int" -> "i32";
            // TODO: what should i do with chars?
            case "Char" -> "String";
            case "Boolean" -> "bool";
            case "String" -> "String";
            default -> null;
        };
        String value = literal.get("value");
        System.out.println("New Ollir Symbol " + value + " " + type);
        return new OllirSymbol(value, type);
    }

    public static OllirSymbol fromSymbol(Symbol s) {
        String type = fromType(s.getType());
        return new OllirSymbol(s.getName(), type);
    }

    public static String fromType(Type type) {
        String arrayPrefix = "";
        String ollirType = "";
        if (type.isArray()) {
            arrayPrefix = "array.";
        }
        Type compare = new Type(type.getName(), false);
        if (compare.equals(JmmBuiltins.JmmInt)) {
            ollirType = "i32";
        } else if (compare.equals(JmmBuiltins.JmmBoolean)) {
            ollirType = "bool";
        } else if (compare.equals(JmmBuiltins.JmmString)) {
            ollirType = "String";
        } else if (compare.equals(JmmBuiltins.JmmVoid)) {
            ollirType = "V";
        } else {
            ollirType = compare.getName();
        }
        return arrayPrefix + ollirType;
    }

    public String toCode() {
        return value + "." + type;
    }

    public OllirSymbol getField() {
        String getter = "getfield(this, " + toCode() + ")";
        return new OllirSymbol(getter, type);

    }

}
