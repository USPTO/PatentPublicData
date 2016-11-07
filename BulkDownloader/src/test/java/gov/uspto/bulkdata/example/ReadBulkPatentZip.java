package gov.uspto.bulkdata.example;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.common.filter.SuffixFilter;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import gov.uspto.patent.model.Patent;

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
            filters.addRule(new SuffixFilter("xml"));
            dumpReader.setFileFilter(filters);
        }

        dumpReader.open();
        if (skip > 0) {
            dumpReader.skip(skip);
        }

        PatentReader patentReader = new PatentReader(patentDocFormat);

        for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
            String xmlDocStr = (String) dumpReader.next();
            //System.out.println("RAW: " + xmlDocStr);

            try (StringReader rawText = new StringReader(xmlDocStr)) {
                Patent patent = patentReader.read(rawText);
                System.out.println(patent.getDocumentId().toText());
                //System.out.println("Patent: " + patent.toString());

                //JsonMapper json = new JsonMapper();
                //JsonObject jsonObj = json.buildJson(patent);
                //System.out.println("JSON: " + jsonObj.toString());
                //System.out.println("JSON: " + json.getPrettyPrint(jsonObj));

            }
        }

        dumpReader.close();
    }
}
