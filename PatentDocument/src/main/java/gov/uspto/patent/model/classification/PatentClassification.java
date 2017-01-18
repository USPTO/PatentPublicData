package gov.uspto.patent.model.classification;

import static gov.uspto.patent.model.classification.ClassificationPredicate.isType;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PatentClassification implements Classification {
    private static Logger LOGGER = LoggerFactory.getLogger(PatentClassification.class);

	private String originalText;
	private Set<PatentClassification> children = new TreeSet<PatentClassification>();
	private Boolean isMainClassification;

	@Override
	public void setTextOriginal(String originalText) {
		this.originalText = originalText;
	}

	@Override
	public String getTextOriginal() {
		return originalText;
	}

	public void setIsMainClassification(Boolean isMainClassification) {
		this.isMainClassification = isMainClassification;
	}

	public Boolean isMainClassification() {
		return isMainClassification;
	}

	public Set<PatentClassification> getChildren() {
		return children;
	}

	public void setChildren(Set<PatentClassification> children) {
		this.children = children;
	}

	public void addChild(PatentClassification classification) {
		if (classification != null) {
			children.add(classification);
		}
	}

	public void addChildren(List<PatentClassification> childClassifications) {
		children.addAll(childClassifications);
	}

	public boolean hasChildWithCode(String symbol) {
		return getChildBySymbol(symbol) != null;
	}

	public PatentClassification getChildBySymbol(String code) {	
		for (PatentClassification classChild : this.children) {
			if (classChild.getTextOriginal().equals(code)) {
				return classChild;
			}
		}
		return null;
	}

	/**
	 * Classification symbol/parts/sections depth.
	 */
	@Override
	public int getDepth() {
		String[] parts = getParts();
		List<String> partList = Arrays.asList(parts);
		return (int) partList.stream().filter(p -> Objects.nonNull(p)).count();
	}

	/**
	 * Returns this classification and all it's children as a single collection.
	 */
	public SortedSet<PatentClassification> flatten() {
		SortedSet<PatentClassification> flat = this.children.stream().map(x -> x.flatten()).flatMap(x -> x.stream())
				.collect(Collectors.toCollection(TreeSet::new));
		flat.add(this);
		return flat;
	}

	/**
	 * Text Representation
	 * 
	 * Either the original text when created from parsing of text else generates normalized form from calling getTextNormalized().
	 */
	@Override
	public String toText() {
		if (getTextOriginal() != null && getTextOriginal().length() > 3) {
			return getTextOriginal();
		} else {
			return getTextNormalized();
		}
	}

	/**
	 * Facets used for Search
	 * 
	 * <pre>
	 * D07B2201/2051 => [0/D, 1/D/D07, 2/D/D07/D07B, 3/D/D07/D07B/D07B2201, 4/D/D07/D07B/D07B2201/D07B22012051]
	 * </pre>
	 */
	@Override
	public String[] toFacet() {
		return ClassificationTokenizer.partsToFacet(getParts());
	}

	/**
	 * Parse Facet back into Classifications
	 */
	@Override
	public <T extends PatentClassification> List<T> fromFacets(List<String> facets, Class<T> classificationClass) {
		return ClassificationTokenizer.fromFacets(facets, classificationClass);
	}
	
	/**
	 * Classification Tree, permutation of all classification parts.
	 * 
	 *<pre>
	 * D07B2201/2051 =>
	 * 
	 * D 07 B 2201 2051
	 * D 07 B 2201
	 * D 07 B
	 * D 07
	 * D
	 *</pre> 
	 */
	@Override
	public String[] getTree() {
		return ClassificationTokenizer.partsToTree(getParts());
	}

	@Override
	public int compareTo(Classification other) {
		int last = this.toText().compareTo(other.toText());
		return last == 0 ? this.toText().compareTo(other.toText()) : last;
	}

    public static <T extends PatentClassification> List<T> fromText(Iterable<String> classificationStrings, Class<T> classificationClass) {
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

    public static <T extends PatentClassification> boolean match(Collection<T> classes, Predicate<PatentClassification> predicate) {
        return classes.stream().anyMatch(predicate);
    }

	public static <T extends PatentClassification> SortedSet<T> filter(Collection<T> classes,
			Predicate<PatentClassification> predicate) {
		return classes.stream().filter(predicate).collect(Collectors.toCollection(TreeSet::new));
	}

	public static <T extends PatentClassification> SortedSet<T> filterByType(Collection<T> classes,
			ClassificationType wantedType) {
		return filter(classes, isType(wantedType));
	}

    public static Set<String> getFacetByType(Collection<PatentClassification> classes, ClassificationType wantedType) {
        Set<PatentClassification> filtered = filter(classes, isType(wantedType));
        Set<String> facets = new LinkedHashSet<String>();
        for(PatentClassification clazz: filtered){
            facets.addAll(Arrays.asList(clazz.toFacet()));
        }
        return facets;
    }
}
