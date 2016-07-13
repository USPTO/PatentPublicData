package gov.uspto.bulkdata.zip;

import java.io.File;
import java.nio.file.Paths;

public class PathRule implements Rule {
	private String parentPath;

	public PathRule(String parentPath) {
		parentPath = parentPath.replaceFirst("^[/\\\\]", ""); // removing leading slashes
		this.parentPath = Paths.get(parentPath).toString();
	}

	public boolean match(File file) {
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
