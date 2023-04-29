package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Optional;

public record OllirSymbol(String value, String type) {
    public static OllirSymbol fromLiteral(JmmNode literal){
        String type = switch (literal.getKind()) {
            case "Int" -> "i32";
            // TODO: what should i do with chars?
            case "Char" -> "String";
            case "Boolean" ->"bool";
            case "String" ->"String";
            default -> null;
        };
        String value = literal.get("value");
        System.out.println("New Ollir Symbol " + value + " " + type);
        return  new OllirSymbol(value,type);
    }
    public String toCode(){
        return value + "." + type;
    }

    public static OllirSymbol noSymbol(){
        return new OllirSymbol(null,null);
    }
}
