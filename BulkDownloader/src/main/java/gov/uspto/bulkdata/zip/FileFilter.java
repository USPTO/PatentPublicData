package gov.uspto.bulkdata.zip;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Criteria for Matching files within ZipReader 
 * 
 * @author Brian G. Feldman (brian.feldman@uspto.gov)
 *
 */
public class FileFilter {
	
	public List<Rule> matchRules = new ArrayList<Rule>();

	public void addRule(Rule... rules){
		for(Rule rule: rules){
			matchRules.add(rule);
		}
	}

	public boolean match(File file) {
		for(Rule rule: matchRules){
			if (!rule.match(file)){
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
