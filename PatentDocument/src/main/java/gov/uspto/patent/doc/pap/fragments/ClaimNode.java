package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Node;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimType;

/**
 * Claim-text can wrap other claim-text elements.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClaimNode extends DOMFragmentReader<List<Claim>> {
	private static final String PATENT_PATH = "//subdoc-claims/claim";

	public ClaimNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public List<Claim> read() {
		List<Claim> claims = new ArrayList<Claim>();

		@SuppressWarnings("unchecked")
		List<Node> claimNodes = document.selectNodes(PATENT_PATH);
		for (Node claimNode : claimNodes) {
			Claim claim = readClaim(claimNode);
			claims.add(claim);
		}

		return claims;
	}

	public Claim readClaim(Node claimNode) {
		String id = claimNode.selectSingleNode("@id").getText();

		Claim claim;
		List<Node> dependentN = claimNode.selectNodes("*/dependent-claim-reference/@depends_on");
		if (dependentN != null && !dependentN.isEmpty()) {
			claim = new Claim(id, claimNode.asXML(), ClaimType.DEPENDENT, textProcessor);
			Set<String> dependentIds = new HashSet<String>();
			for (Node refNode : dependentN) {
				dependentIds.add(refNode.getText());
			}
			claim.setDependentIds(dependentIds);
		} else {
			claim = new Claim(id, claimNode.asXML(), ClaimType.INDEPENDENT, textProcessor);
		}

		return claim;
	}

}
