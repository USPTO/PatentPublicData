package gov.uspto.common.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class EqualFilter implements FileFilter, StringFilter {
    private final String[] wanted;

    public EqualFilter(String... wanted) {
        this.wanted = wanted;
    }

    @Override
    public boolean accept(String valueStr) {
        for (String wantedValue : wanted) {
            if (wantedValue.equals(valueStr)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File file) {
        return accept(file.getName());
    }

    public String[] getWanted() {
        return wanted;
    }

    @Override
    public String toString() {
        return "EqualFilter [wanted=" + Arrays.toString(wanted) + "]";
    }

}