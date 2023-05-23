package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.ast.JmmNode;

public enum IdentifierType {
    ClassField("classField"),
    LocalVariable("localVariable"),
    MethodParameter("methodParameter"),

    ClassType("classType");

    private final String type;

    private IdentifierType(String type) {
        this.type = type;

    }


    public void putIdentiferType(JmmNode node) {
        node.put("idType", type);

    }
    public static IdentifierType fromJmmNode(JmmNode node) {
        // Im sure this could be simplified
        String type = node.get("idType");
        return switch (type) {
            case "classField" -> ClassField;
            case "localVariable" -> LocalVariable;
            case "methodParameter" -> MethodParameter;
            case "classType" -> ClassType;
            default -> null;
        };

    }
}
