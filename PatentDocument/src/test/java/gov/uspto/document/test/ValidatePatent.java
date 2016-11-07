package gov.uspto.document.test;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import gov.uspto.patent.model.Patent;

public class ValidatePatent {

    public static void methodsReturnNonNull(Patent patent) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        for (Method method : Patent.class.getMethods()) {
            // patent.getDocumentId().toText() + " has a null field: " + field.getName()
            if (method.getName().startsWith("get")){
                
                Object ret = method.invoke(patent, null);
                if (ret == null){
                    System.err.println("!! This Patent.class method is returning null: '" + method.getName() + "()' for Patent id: " + patent.getDocumentId().toText());
                }
                assertNotNull(ret);
            }
        }
    }
}
