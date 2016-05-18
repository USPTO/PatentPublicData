package gov.uspto.bulkdata;
import java.io.File;
import java.nio.file.Paths;

/**
 * Criteria for Matching files within ZipReader 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FileFilter {
	private String fileSuffix;
	private String fileName;
	private String parentPath;

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getParentPath() {
		return parentPath;
	}

	/**
	 * Parent Path
	 * 
	 * @param parentPath - Full path to the directory which holds the file
	 * 
	 */
	public void setParentPath(String parentPath) {
		/*
		 *  Utilizing path to normalize different ways of writing the path.
		 *  for example the following are equal: 
		 *      corpus\\patents\\subdir
		 *  	corpus\\patents\\subdir\\
		 *      corpus/patents/subdir
		 *  	corpus/patents/subdir/ 
		 */
		parentPath = parentPath.replaceFirst("^[/\\\\]", ""); // removing leading slashes
		this.parentPath = Paths.get(parentPath).toString();
	}

	public boolean match(File file) {
		boolean found = false;

		if (parentPath != null) {
			if (file.getParent() != null && file.getParent().equals(parentPath)){
				found = true;
			}
			else {
				return false;
			}
		}

		if (fileName != null) {
			if (file.getName().equals(fileName)){
				found = false;
			}
			else {
				return false;
			}
		}

		if (fileSuffix != null) {
			if (file.getName().endsWith(fileSuffix)){
				found = true;
			}
			else {
				return false;
			}
		}

		return found;
	}

}
