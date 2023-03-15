package pt.up.fe.comp2023.analysis.symboltable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MyJmmSymbolTable {
    List<String> imports;
    List<ClassSymbolTable> classes;

    public MyJmmSymbolTable(List<String> imports, List<ClassSymbolTable> classes) {
        this.imports = imports;
        this.classes = classes;
    }

    public String tableToString(String indentation) {
        String showImports = (this.imports.size() > 0) ? "Imports:\n" : "";
        String showClasses = (this.classes.size() == 1) ? "Class:\n" : "Classes:\n";
        String thisIndentation = indentation + "  ";
        StringBuilder classes = new StringBuilder();
        for (ClassSymbolTable c : this.classes) {
            classes.append(c.tableToString(thisIndentation));
        }
        StringBuilder imports = new StringBuilder();
        for (String c : this.imports) {
            imports.append(thisIndentation).append(c).append("\n");
        }
        return showImports + imports + showClasses + classes;
    }

}
