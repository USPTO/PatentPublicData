package gov.uspto.patent.doc.xml.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import gov.uspto.parser.dom4j.DOMFragmentReader;
import gov.uspto.patent.TextProcessor;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimType;

/**
 * 
 * Claim-text can wrap other claim-text elements.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class ClaimNode extends DOMFragmentReader<List<Claim>> {
	private static final XPath CLAIMSXP = DocumentHelper.createXPath("/*/claims/claim");
	private static final XPath CLAIMIDXP = DocumentHelper.createXPath("@id");
	private static final XPath CLAIMREFSXP = DocumentHelper.createXPath("*/claim-ref/@idref");

	public ClaimNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public List<Claim> read() {
		List<Claim> claims = new ArrayList<Claim>();

		List<Node> claimNodes = CLAIMSXP.selectNodes(document);
		for (Node claimN : claimNodes) {
			Claim claim = readClaim(claimN);
			claims.add(claim);
		}

		return claims;
	}

	public Claim readClaim(Node claimNode) {

		String id = CLAIMIDXP.selectSingleNode(claimNode).getText();
		
		// Node claimTypeN = claimNode.selectSingleNode("@claim-type");
		// String claimType = claimTypeN != null ? claimTypeN.getText() : null;

		Claim claim;
		List<Node> dependentN = CLAIMREFSXP.selectNodes(claimNode);
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
