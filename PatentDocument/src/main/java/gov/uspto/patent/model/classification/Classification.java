package gov.uspto.patent.model.classification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

public abstract class Classification implements Comparable<Classification> {

	private ClassificationType type;
	private String originalText;
	private Set<Classification> children = new TreeSet<Classification>();
	private Boolean isMainClassification;

	public Classification(ClassificationType type, String originalText) {
		Preconditions.checkNotNull(type, "ClassificationType can not be null");
		this.type = type;
		this.originalText = originalText;
	}

	public Boolean isMainClassification() {
		return isMainClassification;
	}

	public void setIsMainClassification(Boolean isMainClassification) {
		this.isMainClassification = isMainClassification;
	}

	public Set<Classification> getChildren() {
		return children;
	}

	public void setChildren(Set<Classification> children) {
		this.children = children;
	}

	public void addChild(Classification classification) {
		if (classification != null) {
			children.add(classification);
		}
	}

	public void addChildren(List<Classification> childClassifications) {
		children.addAll(childClassifications);
	}

	public ClassificationType getType() {
		return type;
	}

	public String getText() {
		return originalText;
	}

	public void setTest(String originalText) {
		this.originalText = originalText;
	}

	public boolean hasChildWithCode(String symbol) {
		return getChildBySymbol(symbol) == null;
	}

	public Classification getChildBySymbol(String code) {
		for (Classification classChild : this.children) {
			if (classChild.getText().equals(code)) {
				return classChild;
			}
		}
		return null;
	}

	/**
	 * Returns this classification and all it's children as a single List.
	 * 
	 * @return
	 */
	public List<Classification> asList() {
		List<Classification> classList = new ArrayList<Classification>();

		classList.add(this);
		for (Classification c : this.children) {
			classList.addAll(c.asList());
		}

		return classList;
	}

	/**
	 * Formats the given raw classification read from the xml and coverts it to
	 * something we can use for a lookup. Converts something like "D 1102" to
	 * "D01102000" a 9 digit value without spaces.
	 *
	 * @param classification
	 * @return
	 */
	/*
	 * private String standardize(String classification) { // String c; //c =
	 * StringUtils.rightPad(classification, 9, "0"); //c =
	 * StringUtils.replaceChars(c, ' ', '0'); //return c;
	 * 
	 * // Right Pad to 9 Characters. String changed = String.format("%1$-9s",
	 * classification);
	 * 
	 * // Replace all spaces with zeros. changed = changed.replace(' ', '0');
	 * 
	 * return changed; }
	 */

	/**
	 * Turns a facet string, as provided by the API into a set of facets that
	 * would match anywhere along the hierarchy. To be specific, it takes
	 * something like this: 2/D01/D01101000/D01102000 And turns it into:
	 * "0/D01", "1/D01/D01101000", "2/D01/D01101000/D01102000"
	 */
	public static List<String> parseFacets(String facet) {
		List<String> sections = new ArrayList<String>();

		String[] subc = facet.split("/");
		subc[0] = ""; // clear off the leading number.
		for (int i = 0; i < subc.length - 1; i++) {
			sections.add(i + Joiner.on("/").join(subc, 0, i + 2)); // StringUtils.join(subc,
																	// "/", 0, i
																	// + 2));
		}
		return sections;
	}

	public static List<String> partsToFacet(String... parts) {
		List<String> facets = new LinkedList<String>();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] == null) {
				break;
			}

			sb.append("").append(parts[i]);
			if (facets.size() > 0) {
				facets.add(facets.get(i - 1) + "/" + sb.toString());
			} else {
				facets.add(sb.toString());
			}
		}

		for (int k = 0; k < facets.size(); k++) {
			facets.set(k, k + "/" + facets.get(k));
		}

		return facets;
	}

	public static List<? extends Classification> getByType(Collection<? extends Classification> classes, ClassificationType type) {
		List<Classification> retClasses = new LinkedList<Classification>();

		for (Classification pclass : classes) {
			if (pclass != null && pclass.getType() == type) {
				retClasses.add(pclass);
			}
		}
		
		return retClasses;
	}

	public static SortedSet<String> getFacetByType(Collection<? extends Classification> classes, ClassificationType type) {
		SortedSet<String> retClasses = new TreeSet<String>();

		for (Classification pclass : classes) {
			if (pclass != null && pclass.getType() == type) {

				List<String> facets = Collections.emptyList();
				switch (type) {
				case CPC:
					facets = ((CpcClassification) pclass).toFacet();
					break;
				case IPC:
					facets = ((IpcClassification) pclass).toFacet();
					break;
				case USPC:
					facets = ((UspcClassification) pclass).toFacet();
					break;
				default:
					break;
				}

				retClasses.addAll(facets);
			}
		}

		return retClasses;
	}

	@Override
	public int compareTo(Classification other) {
		int last = this.originalText.compareTo(other.originalText);
		return last == 0 ? this.originalText.compareTo(other.originalText) : last;
	}
	
	 /**
     * Collapse Classification Facets to list of Specific Classifications
     * 
     * Returns the classification list extracted from the faceted string stored in solr
     * 
     * @param cpcVal // e.g. {0/A45B, 0/E04H, 1/A45B/A45B17, 1/E04H/E04H12, 2/A45B/A45B17/A45B1700, 2/E04H/E04H12/E04H122284 }
     * @return       // {A45B1700, E04H122284}
     */
    public static List<String> getMostSpecificClasses(List<String> facets) {
        List<String> leafClasses = new ArrayList<String>();

        String largestNode = facets.get(facets.size() - 1).split("/")[0];
        for (int i = facets.size() - 1; i > 0; i--) {
            String nodes[] = facets.get(i).split("/");
            if (!nodes[0].equals(largestNode)) {
                break;
            }
            leafClasses.add(nodes[nodes.length - 1]);
        }

        return leafClasses;
    }
}
