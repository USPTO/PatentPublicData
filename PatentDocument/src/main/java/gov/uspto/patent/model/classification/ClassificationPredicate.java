package gov.uspto.patent.model.classification;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import gov.uspto.patent.model.Patent;

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

	public static Predicate<PatentClassification> isContained(PatentClassification wantedClass) {
		return p -> p.getType() == wantedClass.getType() && wantedClass.isContained(p);
	}

	public static Predicate<PatentClassification> isType(ClassificationType wantedType) {
		return p -> p.getType() == wantedType;
	}

	public static Set<PatentClassification> filterType(Collection<PatentClassification> list, Predicate<PatentClassification> predicate) {
		return list.stream().filter(predicate).collect(Collectors.toCollection(TreeSet::new));
	}

	public static boolean matchList(Collection<PatentClassification> list, Predicate<PatentClassification> predicate) {
		return list.stream().anyMatch(predicate);
	}

	public static boolean matchPatent(Patent patent, Predicate<PatentClassification> predicate) {
		if (patent == null || patent.getClassification() == null){
			return false;
		}
		return patent.getClassification().parallelStream().anyMatch(predicate);
	}
}
