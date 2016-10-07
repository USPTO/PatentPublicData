package gov.uspto.common.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileIterator {
    
    private FileIterator(){}

    /**
     * Iterator for Files in a Directory.
     * 
     * @param directory
     * @return
     * @throws FileNotFoundException 
     */
    public static Iterator<File> getFileIterator(File file) throws FileNotFoundException {
        if (file.isDirectory()){
            return FileUtils.iterateFiles(file, new String[] { "zip" }, true);
        } else {
            return FileIterator.getFileIterator(Arrays.asList(new String[]{file.getAbsolutePath()}));
        }
    }

    /**
     * Iterator for Files
     *
     * If directory is passed, iterator includes all files in the directory.
     * If single file is passed, iterator includes the single file.
     *
     * @param directory
     * @param
     * @return
     * @throws FileNotFoundException 
     */
    public static Iterator<File> getFileIterator(File file, String[] fileExts, boolean recursive) throws FileNotFoundException {
        if (file.isDirectory()){
            return FileUtils.iterateFiles(file, fileExts, recursive);
        } else {
            return FileIterator.getFileIterator(Arrays.asList(new String[]{file.getAbsolutePath()}));
        }
    }

    /**
     * Iterator for File names passed in, Single or Multiple.
     * 
     * @param filePathStrings
     * @return
     * @throws FileNotFoundException
     */
    private static Iterator<File> getFileIterator(String... filePathStrings) throws FileNotFoundException {
        return FileIterator.getFileIterator(Arrays.asList(filePathStrings));
    }

    /**
     * Iterator for File names passed in as Java Collection.
     * 
     * @param directory
     * @return
     */
    private static Iterator<File> getFileIterator(Collection<String> fileNames) throws FileNotFoundException {
        List<File> filenames = new ArrayList<File>(fileNames.size());
        for (String filename : fileNames) {
            File file = new File(filename);
            if (file.isFile() && file.canRead()) {
                filenames.add(file);
            } else {
                throw new FileNotFoundException("Can not find or read file: " + file.getAbsolutePath());
            }
        }
        return filenames.iterator();
    }

}
