package gov.uspto.bulkdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;

public class RecordReader {

	private final BulkReaderArguments bulkReaderArgs;

	public RecordReader(BulkReaderArguments args) {
		this.bulkReaderArgs = args;
	}

	public void read(RecordProcessor processor) throws PatentReaderException, IOException, DocumentException {
		File inputFile = bulkReaderArgs.getInputFile().toFile();
		Path outputFilePath = bulkReaderArgs.getOutputFile();

		FileFilterChain filters = new FileFilterChain();
        DumpReader dumpReader;
        if (bulkReaderArgs.isApsPatent()) {
            dumpReader = new DumpFileAps(inputFile);
            //filter.addRule(new SuffixFileFilter("txt"));
        } else {
            PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);
            switch (patentDocFormat) {
            case Greenbook:
                //aps = true;
                dumpReader = new DumpFileAps(inputFile);
                //filters.addRule(new PathFileFilter(""));
                //filters.addRule(new SuffixFilter("txt"));
                break;
            default:
                //DumpFileXml2 dumpXml = new DumpFileXml2(inputFile);
                DumpFileXml dumpXml = new DumpFileXml(inputFile);
    			if (PatentDocFormat.Pap.equals(patentDocFormat) || bulkReaderArgs.addHtmlEntities()) {
    				dumpXml.addHTMLEntities();
    			}
                dumpReader = dumpXml;
                filters.addRule(new SuffixFileFilter(new String[] {"xml", "sgm", "sgml"}));
            }
            dumpReader.setFileFilter(filters);
        }

        Writer writer = null;
        if (outputFilePath != null) {
            writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(outputFilePath.toFile()), Charset.forName("UTF-8")));
        } else {
            writer = new BufferedWriter(new OutputStreamWriter(System.out, Charset.forName("UTF-8")));
        }

        read(dumpReader, processor, writer);
	}

    public void read(DumpReader dumpReader, RecordProcessor processor, Writer writer) throws PatentReaderException, IOException, DocumentException {
 
    	dumpReader.open();
    	dumpReader.skip(bulkReaderArgs.getSkipRecordCount());
    	long sucessCount = 0;
    	long failCount = 0;
    	
        for (int checked=1; dumpReader.hasNext(); checked++) {
        	String sourceTxt = dumpReader.getFile().getName() + ":" + dumpReader.getCurrentRecCount();
 
        	String rawRecord;
            try {
            	rawRecord = dumpReader.next();
            } catch (NoSuchElementException e) {
                break;
            }

            Boolean success = processor.process(sourceTxt, rawRecord, writer);
            if (success) { sucessCount++; } else { failCount++; }

        	if (checked == bulkReaderArgs.getRecordReadLimit() || 
        			sucessCount == bulkReaderArgs.getSucessLimit() || 
        			failCount == bulkReaderArgs.getFailLimit()) {
        		break;
        	}
        }

        writer.close();
        dumpReader.close();
    }

	public PatentReader getPatentReader() {
		return new PatentReader(getPatentDocFormat());
	}

	public PatentDocFormat getPatentDocFormat() {
		File inputFile = bulkReaderArgs.getInputFile().toFile();
		return new PatentDocFormatDetect().fromFileName(inputFile);
	}
}
