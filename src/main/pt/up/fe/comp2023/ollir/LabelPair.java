package pt.up.fe.comp2023.ollir;

public class LabelPair {
    private int id;

    final private String name;

    public LabelPair(String name) {
        this.name = name;
        id = 0;
    }

    public void next() {
        id++;
    }

    public String enter() {
        return name + "_enter_" + id;
    }

    public String end() {
        return name + "_end_" + id;
    }
}
