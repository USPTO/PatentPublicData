package gov.uspto.patent.model.classification;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Classification Predicate
 * 
 * Predicate chaining:
 *
 *{@code
 *  predicate1.or(predicate2);
 *}
 * 
 * Over a Collection "Stream":
 *{@code   
 * 	list.parallelStream().anyMatch(predicate);
 * 	list.stream().filter(predicate);
 * }
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 * 
 * @see java.util.stream
 * @see java.util.function.Predicate
 */
public class ClassificationPredicate {

    public static <T extends PatentClassification> Predicate<PatentClassification> isContained(T wantedClass) {
        return p -> p.getType() == wantedClass.getType() && wantedClass.isContained(p);
    }

    public static <T extends PatentClassification> Predicate<PatentClassification> isContained(Iterable<T> wantedClasses) {
        Predicate<PatentClassification> predicate = null;
        Iterator<T> classIt = wantedClasses.iterator();
        if (classIt.hasNext()) {
            predicate = isContained(classIt.next());
        }
        while (classIt.hasNext()) {
            predicate.or(isContained(classIt.next()));
        }
        return predicate;
    }

    public static <T extends PatentClassification> Predicate<PatentClassification> isContained(Iterable<String> wantedClassStrs, Class<T> classificationClass) {
        List<T> patentClasses = PatentClassification.fromText(wantedClassStrs, classificationClass);
        return isContained(patentClasses);
    }

    public static Predicate<PatentClassification> isType(ClassificationType wantedType) {
        return p -> p.getType() == wantedType;
    }
}
