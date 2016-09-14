package gov.uspto.parser.dom4j.keyvalue;

public class KeyValue {
    private final String key;
    private String value;

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value.trim();
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    
    public void setValue(String value){
        this.value = value.trim();
    }

    public void appendValue(String value){
        this.value = this.value + " " + value.trim();
    }

    @Override
    public String toString() {
        return "KeyValue [key=" + key + ", value=" + value + "]";
    }
}
