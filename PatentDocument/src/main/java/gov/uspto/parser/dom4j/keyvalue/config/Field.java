package gov.uspto.parser.dom4j.keyvalue.config;

public class Field {
    private String name;
    private boolean isAchor;

    public Field(String name, boolean isAchor) {
        this.name = name;
        this.isAchor = isAchor;
    }

    public String getName() {
        return name;
    }

    public boolean isAchor() {
        return isAchor;
    }

    @Override
    public String toString() {
        return "Field [name=" + name + ", isAchor=" + isAchor + "]";
    }
}
