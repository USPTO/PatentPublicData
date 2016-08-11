package gov.uspto.common.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class NameEqualFileFilter implements FileFilter {
	private String[] filenames;

	public NameEqualFileFilter(String... filenames) {
		this.filenames = filenames;
	}

	public boolean accept(File file) {
		for (String filename : filenames) {
			if (file.getName().equals(filename)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "NameRule [filenames=" + Arrays.toString(filenames) + "]";
	}

}