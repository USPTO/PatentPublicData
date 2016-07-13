package gov.uspto.bulkdata.zip;

import java.io.File;

public interface Rule {
	boolean match(File file);
}
