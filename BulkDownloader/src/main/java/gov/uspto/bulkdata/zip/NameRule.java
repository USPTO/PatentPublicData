package gov.uspto.bulkdata.zip;

import java.io.File;

public class NameRule implements Rule {
	private String filename;

	public NameRule(String filename) {
		this.filename = filename;
	}

	public boolean match(File file) {
		if (file.getName().equals(file.getName())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "NameRule [filename=" + filename + "]";
	}
}