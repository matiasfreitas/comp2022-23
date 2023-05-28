package pt.up.fe.comp2023.ollir;

public class LabelPair {
    private int id;

    final private String name;
    final private String first;
    final private String second;

    public LabelPair(String name, String first, String second) {
        this.name = name;
        this.first = first;
        this.second = second;
        id = 0;
    }

    public void next() {
        id++;
    }

    public String first() {
        return name + "_" + first + "_" + id;
    }

    public String second() {
        return name + "_" + second + "_" + id;
    }
}
