package gov.uspto.patent.doc.greenbook.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimType;

public class ClaimNode extends DOMFragmentReader<List<Claim>> {

	private static final String CLAIM_CHILDREN_PATH = "/DOCUMENT/CLMS/*|/DOCUMENT/DCLM/*";

	// REGEX to Remove number in front of sentence.
	private static final Pattern LEADING_NUM = Pattern.compile("^[1-9][0-9]?\\.?\\s+(?=[A-Z])");
	private static final Pattern CLAIM_REF = Pattern.compile("\\bclaim ([0-9](?:-[0-9])?)\\b");

	public ClaimNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public List<Claim> read() {
		List<Claim> claims = new ArrayList<Claim>();
		
		@SuppressWarnings("unchecked")
		List<Node> childNodes = document.selectNodes(CLAIM_CHILDREN_PATH);

		String currentClaimNum = "";
		StringBuilder stb = new StringBuilder();
		for (Node childN : childNodes) {
			
			if (childN.getName().equals("NUM")) {
				// Close off claim.
				if (!currentClaimNum.equals("")) {
					String claimText = clean(stb.toString().trim());
					Claim claim = createClaim(currentClaimNum, claimText);
					claims.add(claim);
				}

				// Initialize for current claim.
				currentClaimNum = childN.getText().trim();
				stb = new StringBuilder();
			} else if (childN.getName().startsWith("PA")) {
				stb.append(" ").append(childN.getText().trim());
			}
		}

		// Add last claim.
		String claimText = clean(stb.toString().trim());
		Claim claim = createClaim(currentClaimNum, claimText);
		claims.add(claim);

		return claims;
	}

	private Claim createClaim(String currentClaimNum, String claimText) {
		Claim claim;
		Set<String> claimRefs = getClaimRef(claimText);
		if (!claimRefs.isEmpty()) {
			claim = new Claim(currentClaimNum, claimText, ClaimType.DEPENDENT, textProcessor);
			claim.setDependentIds(claimRefs);
		} else {
			claim = new Claim(currentClaimNum, claimText, ClaimType.INDEPENDENT, textProcessor);
		}
		return claim;
	}

	private Set<String> getClaimRef(String claimText) {
		Set<String> claimRefs = new HashSet<String>();
		Matcher match = CLAIM_REF.matcher(claimText);
		while (match.find()) {
			String id = match.group(1);
			claimRefs.add(id);
		}
		return claimRefs;
	}

	private String clean(String text) {
		return LEADING_NUM.matcher(text).replaceAll("");
	}
}
