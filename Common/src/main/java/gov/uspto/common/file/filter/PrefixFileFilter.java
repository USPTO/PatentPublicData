package gov.uspto.common.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class PrefixFileFilter implements FileFilter {
	private String[] filePrefixs;

	public PrefixFileFilter(String... filePrefixs) {
		this.filePrefixs = filePrefixs;
	}

	public boolean accept(File file) {
		for (String filePrefix : filePrefixs) {
			if (file.getName().startsWith(filePrefix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SuffixRule [filePrefixs=" + Arrays.toString(filePrefixs) + "]";
	}
}