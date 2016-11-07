package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class PrefixFilter implements FileFilter, StringFilter {
    private final String[] prefixes;

    public PrefixFilter(String... prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public boolean accept(String valueStr) {
        for (String prefix : prefixes) {
            if (valueStr.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File file) {
        return accept(file.getName());
    }

    @Override
    public String toString() {
        return "PrefixFilter [prefixes=" + Arrays.toString(prefixes) + "]";
    }
}