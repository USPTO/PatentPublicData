package gov.uspto.bulkdata.zip;

import java.io.File;

public class SuffixRule implements Rule {
	private String fileSuffix;

	public SuffixRule(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public boolean match(File file) {
		if (file.getName().endsWith(fileSuffix)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "SuffixRule [fileSuffix=" + fileSuffix + "]";
	}
}