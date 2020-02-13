package gov.uspto.patent.doc.pap.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.Attribute ;

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

	private static final XPath CLAIMXP = DocumentHelper
			.createXPath("/patent-application-publication/subdoc-claims/claim");
	private static final XPath DEPENDANCE_XP = DocumentHelper.createXPath("*/dependent-claim-reference/@depends_on");

	public ClaimNode(Document document, TextProcessor textProcessor) {
		super(document, textProcessor);
	}

	@Override
	public List<Claim> read() {
		List<Claim> claims = new ArrayList<Claim>();

		List<Node> claimNodes = CLAIMXP.selectNodes(document);
		for (Node claimNode : claimNodes) {
			Claim claim = readClaim(claimNode);
			claims.add(claim);
		}

		return claims;
	}

	public Claim readClaim(Node claimNode) {
		//String id = claimNode.selectSingleNode("@id").getText();
		Attribute idA = ((Element) claimNode).attribute("id");
		String id = idA.getStringValue();

		Claim claim;
		List<Node> dependentN = DEPENDANCE_XP.selectNodes(claimNode);
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
