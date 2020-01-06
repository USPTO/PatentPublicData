package gov.uspto.patent.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import gov.uspto.patent.FreetextField;
import gov.uspto.patent.TextProcessor;
import joptsimple.internal.Strings;

public class Claim extends FreetextField {
	private String id;
	private String rawText;
	private ClaimType claimType;
	private Set<String> dependentIds; // Parent claims this claim is dependent on.
	private Set<Claim> childClaims = new LinkedHashSet<Claim>();
	private int claimTreeLevel = -1;

	// REGEX to remove number in front of claim text.
	private static final Pattern LEADING_NUM = Pattern.compile("^[1-9][0-9]?\\.?\\s+(?=[A-Z])");

	public Claim(String id, String rawClaimText, ClaimType claimType, TextProcessor formatedTextProcessor) {
		super(formatedTextProcessor);
		this.id = id;
		this.rawText = rawClaimText;
		this.claimType = claimType;
	}

	public boolean isDependent() {
		return (claimType.equals(ClaimType.DEPENDENT));
	}

	/**
	 * Get Dependent IDs
	 * 
	 * IDs of parent claims of which this claim is dependent
	 * 
	 * @return
	 */
	public Set<String> getDependentIds() {
		return dependentIds;
	}

	/**
	 * Set Dependent IDs
	 * 
	 * IDs of parent claims of which this claim is dependent
	 * 
	 * @return
	 */
	public void setDependentIds(Set<String> dependentIds) {
		this.dependentIds = dependentIds;
	}

	public Collection<Claim> getChildClaims() {
		return childClaims;
	}

	public void addChildClaim(Claim childClaim) {
		this.childClaims.add(childClaim);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setClaimTreeLevel(int claimTreeLevel) {
		this.claimTreeLevel = claimTreeLevel;
	}

	public int getClaimTreeLevel() {
		return claimTreeLevel;
	}

	public void setClaimType(ClaimType claimType) {
		this.claimType = claimType;
	}

	public ClaimType getClaimType() {
		return claimType;
	}

	@Override
	public void setRawText(String fieldRawText) {
		this.rawText = fieldRawText;
	}

	@Override
	public String getRawText() {
		return rawText;
	}

	@Override
	public String getPlainText() {
		String text = super.getPlainText();
		text = LEADING_NUM.matcher(text.trim()).replaceAll("");

		return text;
	}

	@Override
	public String getSimpleHtml() {
		String text = super.getSimpleHtml();
		text = LEADING_NUM.matcher(text.trim()).replaceAll("");

		StringBuilder stb = new StringBuilder().append("<p class=\"claim\" id=\"").append(id).append("\"");
		if (dependentIds != null) {
			stb.append(" depends=\"").append(Strings.join(getDependentIds(), " ")).append("\"");
		}
		stb.append(">").append(text);

		// Note forcing a period with each claim.
		if (!text.endsWith(".")) {
			stb.append(".\n");
		}
		stb.append("</p>");

		return stb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Claim) || this.id == null) {
			return false;
		} else {
			Claim other = (Claim) o;
			return this.id.equals(other.getId());
		}
	}

	@Override
	public String toString() {
		return "Claim [id=" + id + ", rawClaimText=" + rawText + ", claimType=" + claimType + ", dependentIds="
				+ dependentIds + " SimpleHtmlText=" + getSimpleHtml() + "]";
	}

}
