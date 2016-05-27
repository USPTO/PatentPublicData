package gov.uspto.patent.xml.items;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import gov.uspto.parser.dom4j.ItemReader;
import gov.uspto.patent.model.entity.ChemicalFormula;

public class ChemistryNode extends ItemReader<List<ChemicalFormula>>{

	private static final String ITEM_NODE_NAME = "chemistry";
	
	public ChemistryNode(Node itemNode) {
		super(itemNode);
	}

	@Override
	public List<ChemicalFormula> read() {
		List<ChemicalFormula> formulas = new ArrayList<ChemicalFormula>();

		List<Node> chems = itemNode.selectNodes("chem");
		for (Node chem: chems){
			//formulas.add();
		}
		
		return formulas;
	}

}
