package gov.uspto.patent.model.entity;

import java.util.List;

public class ChemicalFormula {

	private String formula;

	public ChemicalFormula(String formula) {
		this.formula = formula;
	}

	public String getFormula() {
		return formula;
	}

	public List<String> getTokens() {
		return null;
	}
}
