package gov.uspto.parser.dom4j.keyvalue.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldIndex {

    private Map<String, IndexEntry> index;

    public FieldIndex(List<FieldGroup> fieldGroups) {
        this.index = generateFieldIndex(fieldGroups);
    }

    public IndexEntry getEntry(String fieldName){
        return index.get(fieldName);
    }

    public boolean isAchor(String fieldName) {
        return index.get(fieldName).getField().isAchor();
    }

    public String groupName(String fieldName) {
        return index.get(fieldName).getFieldGroup().getName();
    }

    private Map<String, IndexEntry> generateFieldIndex(List<FieldGroup> fieldGroups) {
        Map<String, IndexEntry> index = new HashMap<String, IndexEntry>();
        for (FieldGroup group : fieldGroups) {
            for (Field field : group.getFields()) {
                if (index.containsKey(field.getName())){
                    System.err.println("Field already defined in another group: " + field.getName() + " : [ " + index.get(field.getName()).getFieldGroup().getName() + ", " + group.getName() + " ]");
                }
                index.put(field.getName(), new IndexEntry(field, group));
            }
        }
        return index;
    }
}
