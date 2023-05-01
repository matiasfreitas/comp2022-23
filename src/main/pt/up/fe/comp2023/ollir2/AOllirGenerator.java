package pt.up.fe.comp2023.ollir2;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.analysis.symboltable.JmmSymbolTable;

import java.util.*;

public abstract class AOllirGenerator<T> extends AJmmVisitor<List<Report>, T> {

    private enum Call {
        InvokeSpecial("invokespecial"),
        InvokeVirtual("invokevirtual"),
        InvokeStatic("invokestatic"),
        PutField("putfield"),
        GetField("getfield");

        private final String name;

        Call(String o) {
            name = o;
        }

        public String getName() {
            return name;
        }
    }

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
        String attribute = node.hasAttribute("value") ? "value" : "varName";
        var field = symbolTable.getFieldTry(node.get(attribute));
        if (field.isEmpty()) {
            return OllirSymbol.noSymbol();
        }
        return OllirSymbol.fromSymbol(field.get());
    }

    protected OllirSymbol fromLocalVariable(JmmNode node) {
        String currentMethod = symbolTable.getCurrentMethod();
        // Isto é porque na gramática tenho varnmae e value :(
        String attribute = node.hasAttribute("value") ? "value" : "varName";
        Optional<Symbol> local = symbolTable.getLocalVariableTry(currentMethod, node.get(attribute));
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
        String ollirType = OllirSymbol.typeFrom(parameter.getType());
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
        var params = Arrays.asList("this", field.toCode(), value.toCode());
        return ollirCall(Call.PutField, params, "V").toCode() + ";\n";

    }

    public OllirSymbol ollirGetField(OllirSymbol lhs) {
        var params = Arrays.asList("this", lhs.value());
        return ollirCall(Call.GetField, params, lhs.type());
    }

    public OllirSymbol ollirInvokeStatic(OllirSymbol object, String method, List<OllirSymbol> params, String type) {
        return ollirInvoke(Call.InvokeStatic, object, method, params, type);
    }

    public OllirSymbol ollirInvokeVirtual(OllirSymbol object, String method, List<OllirSymbol> params, String type) {
        return ollirInvoke(Call.InvokeVirtual, object, method, params, type);
    }

    private OllirSymbol ollirInvoke(Call t, OllirSymbol object, String method, List<OllirSymbol> params, String type) {
        // Becuase asList returns a fixed size list
        var ollirParams = new ArrayList<>(Arrays.asList(object.toCode(), methodName(method)));
        if (params != null) {
            for (var s : params) {
                ollirParams.add(s.toCode());
            }
        }
        return ollirCall(t, ollirParams, type);
    }

    private OllirSymbol ollirCall(Call t, List<String> params, String type) {
        var ollir = t.getName() + "(" + formatArguments(params) + ")";
        return new OllirSymbol(ollir, type);
    }

    public String methodName(String methodName) {
        return '"' + methodName + '"';
    }

    public String ollirInvokeConstructor(String object, List<String> arguments) {
        var args = new LinkedList<>(Arrays.asList(object, methodName("<init>")));
        if (arguments != null)
            args.addAll(arguments);
        return ollirCall(Call.InvokeSpecial, args, "V").toCode() + ";\n";
    }

    public String spaceBetween(List<String> tokens) {
        return interSperse(tokens, " ");
    }

    public String formatArguments(List<String> arguments) {
        return interSperse(arguments, ", ");
    }

    public String interSperse(List<String> tokens, String between) {
        var res = new StringBuilder();
        int i = 0;
        for (; i < tokens.size() - 1; i++) {
            String atI = tokens.get(i);
            if (atI != null && !atI.equals("")) {
                res.append(atI).append(between);
            }
        }
        if (i < tokens.size()) {
            res.append(tokens.get(i));
        }
        return res.toString();
    }
}
