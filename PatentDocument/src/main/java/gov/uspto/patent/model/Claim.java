package gov.uspto.patent.model;

import java.util.Set;
import java.util.regex.Pattern;

import gov.uspto.patent.TextProcessor;

public class Claim {
	private String id;
	private String claimText;
	private ClaimType claimType;
	private TextProcessor formatedTextProcessor;
	private Set<String> dependentIds;

	// REGEX to remove number in front of claim text.
	private static final Pattern LEADING_NUM = Pattern.compile("^[1-9][0-9]?\\.?\\s+(?=[A-Z])");

	public Claim(String id, String rawClaimText, ClaimType claimType, TextProcessor formatedTextProcessor) {
		this.id = id;
		this.claimText = rawClaimText;
		this.claimType = claimType;
		this.formatedTextProcessor = formatedTextProcessor;
	}

	public boolean isDependent(){
		return (claimType.equals(ClaimType.DEPENDENT));
	}
	
	public Set<String> getDependentIds() {
		return dependentIds;
	}

	public void setDependentIds(Set<String> dependentIds) {
		this.dependentIds = dependentIds;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClaimText() {
		return claimText;
	}

	public void setText(String claimText) {
		this.claimText = claimText;
	}

	public void setClaimType(ClaimType claimType) {
		this.claimType = claimType;
	}

	public ClaimType getClaimType() {
		return claimType;
	}

	public String getProcessedText(){
		String text = formatedTextProcessor.getProcessText(claimText);
		text = LEADING_NUM.matcher(text.trim()).replaceAll("");
		return "<p>"+text+".\n</p>";
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof Claim)) {
			return false;
		}

		Claim other = (Claim) o;
		if (this.id != null && this.id != null) {
			if (this.id.equals(other.getId())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "Claim [id=" + id + ", claimText=" + claimText + ", claimType=" + claimType + ", dependentIds="
				+ dependentIds + "]";
	}
}
