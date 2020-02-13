package gov.uspto.patent.model.classification;

import static gov.uspto.patent.model.classification.ClassificationPredicate.isType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PatentClassification implements Classification {

	private static Logger LOGGER = LoggerFactory.getLogger(PatentClassification.class);

	private final String originalText;
	private final boolean mainOrInventive;

	PatentClassification(String originalText, boolean mainOrInventive){
		this.originalText = originalText;
		this.mainOrInventive = mainOrInventive;
	}

	@Override
	public String getTextOriginal() {
		return originalText;
	}

	public boolean isMainOrInventive() {
		return this.mainOrInventive;
	}

	/**
	 * Text Representation
	 * 
	 * Either the original text when created from parsing of text else generates
	 * normalized form from calling getTextNormalized().
	 */
	@Override
	public String toText() {
		if (getTextOriginal() != null && getTextOriginal().length() > 3) {
			return getTextOriginal();
		} else {
			return getTextNormalized();
		}
	}

	@Override
	public int compareTo(Classification other) {
		int last = this.toText().compareTo(other.toText());
		return last == 0 ? this.toText().compareTo(other.toText()) : last;
	}

	public static <T extends PatentClassification> List<T> fromText(Iterable<String> classificationStrings,
			Class<T> classificationClass) {
		List<T> retClasses = new ArrayList<T>();
		for (String textClass : classificationStrings) {
			try {
				T classification = classificationClass.newInstance();
				classification.parseText(textClass);
				retClasses.add(classification);
			} catch (ParseException | InstantiationException | IllegalAccessException e) {
				LOGGER.error("Failed to parse provided Classification: " + textClass, e);
			}
		}
		return retClasses;
	}

	public static <T extends PatentClassification> boolean match(Collection<T> classes,
			Predicate<PatentClassification> predicate) {
		return classes.stream().anyMatch(predicate);
	}

	public static <T extends PatentClassification> SortedSet<T> filter(Collection<T> classes,
			Predicate<PatentClassification> predicate) {
		return classes.stream().filter(predicate).collect(Collectors.toCollection(TreeSet::new));
	}

	/**
	 * Filter Collection of Classifications by ClassificationType
	 * 
	 * @param classes
	 * @param wantedType
	 * @return SortedSet
	 */
	public static <T extends PatentClassification> Map<ClassificationType, Set<PatentClassification>> groupByType(
			Collection<PatentClassification> classes) {
		return classes.stream()
				.collect(Collectors.groupingBy(PatentClassification::getType, TreeMap::new, Collectors.toSet()));
	}

	/**
	 * Filter Collection of Classifications by ClassificationType
	 * 
	 * @param classes
	 * @param wantedType
	 * @return SortedSet
	 */
	public static <T extends PatentClassification> SortedSet<T> filterByType(Collection<PatentClassification> classes,
			ClassificationType wantedType) {

		return filterByType(classes, wantedType.getJavaClass());
	}

	/**
	 * Filter Collection of Classifications by ClassificationType
	 * 
	 * @param classes
	 * @param         class
	 * @return SortedSet
	 */
	public static <T extends PatentClassification> SortedSet<T> filterByType(Collection<PatentClassification> classes,
			Class<T> wantedClass) {
		return classes.stream().filter(wantedClass::isInstance).map(wantedClass::cast)
				.collect(Collectors.toCollection(TreeSet::new));
	}

	public static Set<String> getFacetByType(Collection<PatentClassification> classes, ClassificationType wantedType) {
		Set<PatentClassification> filtered = filter(classes, isType(wantedType));
		Set<String> facets = new LinkedHashSet<String>();
		for (PatentClassification clazz : filtered) {
			facets.addAll(clazz.getTree().getLeafFacets());
		}
		return facets;
	}

	public static <T extends PatentClassification> Set<String> getFacet(Collection<T> classes) {
		if (classes == null) {
			return Collections.emptySet();
		}
		Set<String> facets = new TreeSet<String>();
		for (PatentClassification clazz : classes) {
			facets.addAll(clazz.getTree().getLeafFacets());
		}
		return facets;
	}
}
