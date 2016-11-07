package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Criteria for Matching files within ZipReader; matches if all provided rules return true.
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FileFilterChain implements FileFilter {

    public List<FileFilter> matchRules = new ArrayList<FileFilter>();

    public void addRule(FileFilter... rules) {
        for (FileFilter rule : rules) {
            matchRules.add(rule);
        }
    }

    @Override
    public boolean accept(File file) {
        for (FileFilter rule : matchRules) {
            if (!rule.accept(file)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "FileFilter [matchRules=" + matchRules + "]";
    }
}
