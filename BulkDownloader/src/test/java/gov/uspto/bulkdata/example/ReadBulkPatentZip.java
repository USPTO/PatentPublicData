package gov.uspto.bulkdata.example;

import java.io.File;
import java.io.IOException;

import gov.uspto.bulkdata.DumpFileAps;
import gov.uspto.bulkdata.DumpFileXml;
import gov.uspto.bulkdata.DumpReader;
import gov.uspto.common.file.filter.FileFilterChain;
import gov.uspto.common.file.filter.PathFileFilter;
import gov.uspto.common.file.filter.SuffixFileFilter;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.JsonMapper;

public class ReadBulkPatentZip {

    public static void main(String... args) throws IOException, PatentReaderException {
        
        File inputFile = new File(args[0]);
        int skip = 100;
        int limit = 1;

        PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);

        DumpReader dumpReader;
        switch (patentDocFormat) {
        case Greenbook:
            dumpReader = new DumpFileAps(inputFile);
            break;
        default:
            dumpReader = new DumpFileXml(inputFile);
            FileFilterChain filters = new FileFilterChain();
            //filters.addRule(new PathFileFilter(""));
            filters.addRule(new SuffixFileFilter("xml"));
            dumpReader.setFileFilter(filters);
        }

        dumpReader.open();
        if (skip > 0) {
            dumpReader.skip(skip);
        }

        for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
            String xmlDocStr = (String) dumpReader.next();
            try (PatentReader patentReader = new PatentReader(xmlDocStr, patentDocFormat)) {
                Patent patent = patentReader.read();
                System.out.println(patent.getDocumentId().toText());

                JsonMapper json = new JsonMapper();
                String jsonStr = json.buildJson(patent);
                System.out.println("JSON: " + jsonStr);
                
                System.out.println("Patent: " + patent.toString());
            }
        }

        dumpReader.close();
    }
}
