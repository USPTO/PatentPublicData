package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;

public class OrFileFilter implements FileFilter {

	private FileFilter[] filters;

	public OrFileFilter(FileFilter... filters) {
		this.filters = filters;
	}

	@Override
	public boolean accept(File pathname) {
		boolean result = false;
		for (FileFilter filter : filters) {
			result = result || filter.accept(pathname);
		}
		return result;
	}

}
