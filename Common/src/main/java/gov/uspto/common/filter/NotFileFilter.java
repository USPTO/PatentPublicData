package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * NotFileFilter negates a filter. 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class NotFileFilter implements FileFilter {

	private FileFilter filter;

    public NotFileFilter(FileFilter filter)
    {
        this.filter = filter;
    }
    
	@Override
	public boolean accept(File pathname) {
		return !this.filter.accept(pathname);
	}

}
