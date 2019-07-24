package gov.uspto.patent.doc.xml.fragments;

import static org.junit.Assert.*;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.junit.Test;

import gov.uspto.patent.doc.xml.FormattedText;
import gov.uspto.patent.model.Claim;
import gov.uspto.patent.model.ClaimType;

public class ClaimNodeTest {

	@Test
	public void ClaimSingle() throws DocumentException {
		String xml = "<xml><claims id=\"claims\">\r\n" + 
				"<claim id=\"CLM-00001\" num=\"00001\">\r\n" + 
				"<claim-text>The ornamental design for a pastry, as shown and described.</claim-text>\r\n" + 
				"</claim>\r\n" + 
				"</claims></xml>";
		
		Document doc = DocumentHelper.parseText(xml);
		
		List<Claim> claims = new ClaimNode(doc, new FormattedText()).read();
		//claims.forEach(System.out::println);

		assertEquals("CLM-00001", claims.get(0).getId());
		assertEquals(ClaimType.INDEPENDENT, claims.get(0).getClaimType());
		assertEquals("The ornamental design for a pastry, as shown and described.", claims.get(0).getPlainText());
	}

	@Test
	public void ClaimDependency() throws DocumentException {
		String xml = "<xml><claims id=\"claims\">\r\n" + 
				"<claim id=\"CLM-00016\" num=\"00016\">\r\n" + 
				"<claim-text>16. The assembly of <claim-ref idref=\"CLM-00015\">claim 15</claim-ref>, further including a receiver coupled to and positioned in said fourth housing, said receiver being operationally coupled to said fourth microprocessor, said receiver being radio frequency enabled</claim-text>\r\n" + 
				"</claim>\r\n" + 
				"<claim id=\"CLM-00017\" num=\"00017\">\r\n" + 
				"<claim-text>17. The assembly of <claim-ref idref=\"CLM-00016\">claim 16</claim-ref>, further including said receiver being radio frequency and infrared light enabled.</claim-text>\r\n" + 
				"</claim>\r\n" + 
				"<claim id=\"CLM-00018\" num=\"00018\">\r\n" + 
				"<claim-text>18. A wheel changing assembly comprising:\r\n" + 
				"<claim-text>a jack comprising a first housing, said first housing being substantially rectangularly box shaped;</claim-text>\r\n" + 
				"<claim-text>a first power module coupled to and positioned in said first housing, said first power module comprising a plurality of first batteries, said first batteries being rechargeable, said first power module comprising two first batteries;</claim-text>\r\n" + 
				"<claim-text>a first microprocessor coupled to and positioned in said first housing, said first microprocessor being operationally coupled to said first power module, said first microprocessor comprising a first communicator, said first communicator being enabled for transmission and receipt of wireless signals, said first communicator being Bluetooth enabled;</claim-text>\r\n" + 
				"<claim-text>a pump coupled to and positioned in said first housing, said pump being operationally coupled to said first microprocessor, said pump being hydraulic;</claim-text>\r\n" + 
				"<claim-text>a lift coupled to and positioned in said first housing, said lift being fluidically coupled to said pump, said lift having a first end coupled to a bottom of said first housing, said lift having a second end selectively extensible through a top of said first housing;</claim-text>\r\n" + 
				"<claim-text>a plate coupled to said second end of said lift, said plate being substantially circular, said plate having an upper surface, said upper surface being textured, such that said plate is configured for abutment to a frame of a vehicle and wherein said upper surface is configured to deter slippage of said plate relative to the frame;</claim-text>\r\n" + 
				"<claim-text>a plurality of wheels coupled to said bottom of said first housing, said plurality of wheels comprising wheels positioned singly proximate to each lower corner of said first housing;</claim-text>\r\n" + 
				"<claim-text>a pinion gear rotationally coupled to said first housing within said recess, said pinion gear being gearedly coupled to said rack gear,</claim-text>\r\n" + 
				"<claim-text>a center sprocket rotationally coupled to said first housing within said recess, said center sprocket being gearedly coupled to said pinion gear,</claim-text>\r\n" + 
				"</claim-text></claim>" + 
				"<claim id=\"CLM-00019\" num=\"00019\">\r\n" + 
				"<claim-text>19. The assembly of <claim-ref idref=\"CLM-00018\">claim 18</claim-ref>, further including said receiver being radio frequency and infrared light enabled.</claim-text>\r\n" + 
				"</claim>" +
				"</claims></xml>";

		Document doc = DocumentHelper.parseText(xml);

		List<Claim> claims = new ClaimNode(doc, new FormattedText()).read();
		//claims.forEach(System.out::println);

		assertEquals(4, claims.size());
		
		assertEquals("CLM-00016", claims.get(0).getId());
		assertEquals(ClaimType.DEPENDENT, claims.get(0).getClaimType());
		assertEquals("CLM-00015", claims.get(0).getDependentIds().iterator().next());

		assertEquals("CLM-00017", claims.get(1).getId());
		assertEquals(ClaimType.DEPENDENT, claims.get(1).getClaimType());
		assertEquals("CLM-00016", claims.get(1).getDependentIds().iterator().next());

		assertEquals("CLM-00018", claims.get(2).getId());
		assertEquals(ClaimType.INDEPENDENT, claims.get(2).getClaimType());

		assertEquals("CLM-00019", claims.get(3).getId());
		assertEquals(ClaimType.DEPENDENT, claims.get(3).getClaimType());
		assertEquals("CLM-00018", claims.get(3).getDependentIds().iterator().next());
	}	
	
}
