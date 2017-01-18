package gov.uspto.patent.model.classification;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

/**
 * Classification Tokenizer
 * 
 * Build facets and tokens for indexing or matching of classifications.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClassificationTokenizer {

	private ClassificationTokenizer() {
	}

	/**
	 * Facets used for Search
	 * 
	 * <pre>
	 * D07B2201/2051 => [0/D, 1/D/D07, 2/D/D07/D07B, 3/D/D07/D07B/D07B2201, 4/D/D07/D07B/D07B2201/D07B22012051]
	 * </pre>
	 */
	public static String[] partsToFacet(String... parts) {
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

		Set<String> facetSet = new LinkedHashSet<String>(facets.size());
		facetSet.addAll(facets);
		return facetSet.toArray(new String[facetSet.size()]);
	}

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
			sections.add(i + Joiner.on("/").join(subc, 0, i + 2));
		}
		return sections;
	}

	/**
	 * Generate List of Classifications from list of Facets.
	 * 
	 * <pre>
	 * List<CpcClassification> cpcClasses = SearchBuilder.fromFacets(classificationFacets, CpcClassification.class);
	 * </pre>
	 * 
	 * @param classificationFacets
	 */
	public static <T extends PatentClassification> List<T> fromFacets(final List<String> classificationFacets,
			Class<T> classificationClass) {
		List<String> specificClasses = ClassificationTokenizer.getMostSpecificClasses(classificationFacets);
		return PatentClassification.fromText(specificClasses, classificationClass);
	}

	/**
	 * Collapse Classification Facets to list of Specific Classifications
	 * 
	 * Returns the classification list extracted from the faceted string stored
	 * in solr
	 * 
	 * @param cpcVal
	 *            // e.g. {0/A45B, 0/E04H, 1/A45B/A45B17, 1/E04H/E04H12,
	 *            2/A45B/A45B17/A45B1700, 2/E04H/E04H12/E04H122284 }
	 * @return // {A45B1700, E04H122284}
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

	/**
	 * Classification Tree, permutation of all classification parts.
	 * 
	 * <pre>
	 * D07B2201/2051 =>
	 * 
	 * D 07 B 2201 2051
	 * D 07 B 2201
	 * D 07 B
	 * D 07
	 * D
	 * 
	 * </pre>
	 * 
	 */
	public static String[] partsToTree(String... parts) {
		String[] result = new String[parts.length];

		int len = parts.length;
		for (int i = 0; i < parts.length; i++, len--) {
			StringBuilder sb = new StringBuilder();
			for (int k = 0; k < len; k++) {
				if (k > 0)
					sb.append(' ');
				sb.append(parts[k]);
			}
			result[i] = sb.toString();
		}
		return result;
	}
}
