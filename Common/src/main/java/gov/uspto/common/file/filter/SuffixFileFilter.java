package gov.uspto.common.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class SuffixFileFilter implements FileFilter {
	private String[] fileSuffixs;

	public SuffixFileFilter(String... fileSuffixs) {
		this.fileSuffixs = fileSuffixs;
	}

	public boolean accept(File file) {
		for (String fileSuffix : fileSuffixs) {
			if (file.getName().endsWith(fileSuffix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SuffixRule [fileSuffixs=" + Arrays.toString(fileSuffixs) + "]";
	}
}