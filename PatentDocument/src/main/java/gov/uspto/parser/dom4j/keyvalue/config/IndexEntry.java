package gov.uspto.parser.dom4j.keyvalue.config;

public class IndexEntry{
    private FieldGroup fieldGroup;
    private Field field;

    public IndexEntry(Field field, FieldGroup FieldGroup){
        this.field = field;
        this.fieldGroup = FieldGroup;
    }

    public FieldGroup getFieldGroup() {
        return fieldGroup;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return "IndexEntry [fieldGroup=" + fieldGroup + ", field=" + field + "]";
    }
    
}