package gov.uspto.parser.dom4j.keyvalue.config;

import java.util.ArrayList;
import java.util.List;

public class FieldGroup {
    private String name;
    private List<Field> fields = new ArrayList<Field>();
    private boolean isMultivalued = false; // multiple occurrence, true if ancorField.

    public FieldGroup(String name) {
        this.name = name;
    }

    public FieldGroup(String name, List<Field> fields) {
        this.name = name;
        this.fields = fields;
    }

    public FieldGroup addField(String... fieldNames) {
        for (String fieldName : fieldNames) {
            addField(fieldName, false);
        }
        return this;
    }

    public FieldGroup addField(String fieldName) {
        return addField(fieldName, false);
    }

    public FieldGroup addField(String name, boolean isAnchor) {
        this.fields.add(new Field(name, isAnchor));
        return this;
    }

    /**
     * Set Ancor Field
     *
     * The achor field denotes the start of an entity, by defining the first fields which occur in the field sequence.
     * 
     * @param name - one or more field names.
     * @return
     */
    public FieldGroup setAncorField(String... name) {
        for(String fieldName: name){
            this.fields.add(new Field(fieldName, true));
        }
        this.isMultivalued = true;
        return this;
    }

    public boolean isMultivalued() {
        return isMultivalued;
    }

    public String getName() {
        return name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public static List<Field> genAnchorFieldList(List<Field> fields) {
        List<Field> achorFields = new ArrayList<Field>();
        for (Field field : fields) {
            if (field.isAchor()) {
                achorFields.add(field);
            }
        }
        return achorFields;
    }

    @Override
    public String toString() {
        return "FieldGroup [name=" + name + ", fields=" + fields + ", isMultivalued=" + isMultivalued + "]";
    }
}
