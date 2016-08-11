package gov.uspto.patent.model;

import java.util.HashSet;
import java.util.Set;

import gov.uspto.patent.TextField;

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
public class Figure implements TextField {

	private Set<String> ids = new HashSet<String>();
	private String rawText;

	public Figure(String rawText, String... ids) {
		for(String id: ids){
			this.ids.add(id);
		}
		this.rawText = rawText;
	}

	public Figure(String rawText, Set<String> ids) {
		this.ids = ids;
		this.rawText = rawText;
	}

	public Set<String> getIds() {
		return ids;
	}

	public boolean hasId(String id) {
		return ids.contains(id);
	}

	@Override
	public void setRawText(String fieldRawText) {
		rawText = fieldRawText;
	}

	@Override
	public String getRawText() {
		return rawText;
	}

	@Override
	public String toString() {
		return "Figure [ids=" + ids + ", rawText=" + rawText + "]";
	}
}
