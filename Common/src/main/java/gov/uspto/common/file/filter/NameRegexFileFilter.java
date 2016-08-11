package gov.uspto.common.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NameRegexFileFilter implements FileFilter {
	private List<Pattern> regexs = new ArrayList<Pattern>();

	public NameRegexFileFilter(String... regexs) {
		for(String regex: regexs){
			this.regexs.add(Pattern.compile(regex));
		}
	}

	public boolean accept(File file) {
		for (Pattern regex : regexs) {
			if(regex.matcher(file.getName()).matches()){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "NameRule [regexs=" + regexs + "]";
	}
}