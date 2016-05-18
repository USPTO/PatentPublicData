package gov.uspto.patent.model.entity;

import java.util.List;

public class MathFormula {

	private String formula;

	public MathFormula(String formula) {
		this.formula = formula;
	}

	public String getFormula() {
		return formula;
	}

	public List<String> getTokens() {
		return null;
	}
}
