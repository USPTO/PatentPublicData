package gov.uspto.common.predicate;

import java.util.function.Predicate;

public class InstancePredicate implements Predicate<Object> {
    private final Class<?> type;
    
    public InstancePredicate(Class<?> type) {
       if (type == null){
            throw new IllegalArgumentException("null type");
       }
       this.type = type;
    }

    @Override
    public boolean test(Object obj) {
        return obj != null && this.type.isInstance(obj);
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InstancePredicate [type=" + type + "]";
    }
}
