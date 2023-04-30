package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.List;
import java.util.Optional;

public abstract class AOllirGenerator<T> extends AJmmVisitor<List<Report>, T> {

    protected JmmSymbolTable symbolTable;

    public AOllirGenerator(JmmSymbolTable s) {
        this.symbolTable = s;
    }


    protected OllirSymbol fromIdentifier(JmmNode node) {
        System.out.println(node.getAttributes());
        IdentifierType idType = IdentifierType.fromJmmNode(node);
        if (idType == null) {
            System.err.println("This node has no  idType it is not being handled in semantics!!");
            System.out.println(node.toTree());
            return OllirSymbol.noSymbol();
        }
        return switch (idType) {
            case ClassField -> fromFieldIdentifier(node);
            case MethodParameter -> fromParameterIdentifier(node);
            case LocalVariable -> fromLocalVariable(node);
            // TODO:
            case ClassType -> null;
        };
    }

    protected OllirSymbol fromFieldIdentifier(JmmNode node) {
        // Isto é porque na gramática tenho varnmae e value :(
        String attribute = node.hasAttribute("value")? "value" : "varName";
        var field = symbolTable.getFieldTry(node.get(attribute));
        if (field.isEmpty()) {
                return OllirSymbol.noSymbol();
        }
        return OllirSymbol.fromSymbol(field.get());
    }

    protected OllirSymbol fromLocalVariable(JmmNode node) {
        String currentMethod = symbolTable.getCurrentMethod();
        Optional<Symbol> local = symbolTable.getLocalVariableTry(currentMethod, node.get("value"));
        // Isto quase de certeza que não vai acontecer o que devo fazer?
        if (local.isEmpty()) {
            // this can't happen
            return OllirSymbol.noSymbol();
        }
        return OllirSymbol.fromSymbol(local.get());
    }

    protected OllirSymbol fromParameterIdentifier(JmmNode node) {
        String currentMethod = symbolTable.getCurrentMethod();
        String identifier = node.get("value");
        var arguments = symbolTable.getParameters(currentMethod);
        int i = 0;
        for (; i < arguments.size(); i++) {
            if (arguments.get(i).getName().equals(identifier)) {
                break;
            }
        }
        Symbol parameter = arguments.get(i);
        String ollirType = OllirSymbol.fromType(parameter.getType());
        if (!symbolTable.isStaticMethod(currentMethod)) {
            // The first argument is this so we increment i
            i++;
        }
        return new OllirSymbol("$" + i + "." + parameter.getName(), ollirType);
    }
    public String ollirAssignment(OllirSymbol lhs, OllirSymbol rhs) {
        var code = new StringBuilder(lhs.toCode());
        code.append(" :=.")
                .append(lhs.type())
                .append(" ")
                .append(rhs.toCode())
                .append(";\n");
        return code.toString();
    }

    public String ollirPutField(OllirSymbol field, OllirSymbol value) {
        var code = new StringBuilder("putfield(this, ");
        code.append(field.toCode())
                .append(", ")
                .append(value.toCode())
                .append(").")
                .append(field.type())
                .append(";\n");
        return code.toString();

    }

    public OllirSymbol ollirGetField(OllirSymbol lhs) {
        String getter = "getfield(this, " + lhs.toCode() + ")";
        return new OllirSymbol(getter, lhs.type());
    }
    public String spaceBetween(List<String> tokens){
        return interSperse(tokens, " ");
    }

    public String formatArguments(List<String> arguments){
        return interSperse(arguments, ", ");
    }
    public String interSperse(List<String> tokens,String between){
        var res = new StringBuilder();
        int i = 0;
        for(;i < tokens.size() - 1; i++){
            String atI = tokens.get(i);
            if(atI != null && !atI.equals("")){
                res.append(atI).append(between);
            }
        }
        if( i < tokens.size()){
            res.append(tokens.get(i));
        }
        return res.toString();
    }
}
