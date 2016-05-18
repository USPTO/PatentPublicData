package gov.uspto.patent.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Referenced Figure
 *
 *<p>
 *The purpose of this class is to capture the figures within the Patent Description sub section for Brief Description of Drawings.
 *</p>
 *
 *<p>
 *Since Figure Descriptions can contain an id range, the id is stored in a set of IDs.
 *</p>
 */
public class Figure {

	private Set<String> ids = new HashSet<String>();
	private String text;

	public Figure(String id, String text) {
		this.ids.add(id);
		this.text = text;
	}

	public Figure(Set<String> ids, String text) {
		this.ids = ids;
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public Set<String> getIds() {
		return ids;
	}

	public boolean hasId(String id) {
		return ids.contains(id);
	}

	@Override
	public String toString() {
		return "Figure [ids=" + ids + ", text=" + text + "]";
	}
}
