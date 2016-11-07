package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Paths;

public class PathFileFilter implements FileFilter {
	private String parentPath;

	public PathFileFilter(String parentPath) {
		parentPath = parentPath.replaceFirst("^[/\\\\]", ""); // removing leading slashes
		this.parentPath = Paths.get(parentPath).toString();
	}

	@Override
	public boolean accept(File file) {
		if (file.getParent() != null && file.getParent().equals(parentPath)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "PathRule [parentPath=" + parentPath + "]";
	}
}
