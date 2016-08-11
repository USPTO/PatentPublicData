package gov.uspto.bulkdata.example;

import java.io.File;
import java.io.IOException;

import gov.uspto.bulkdata.DumpFileAps;
import gov.uspto.bulkdata.DumpFileXml;
import gov.uspto.bulkdata.DumpReader;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.PatentType;
import gov.uspto.patent.PatentTypeDetect;
import gov.uspto.patent.model.Patent;

public class ReadBulkPatentZip {

	public void main() throws IOException, PatentReaderException{
		File inputFile = new File("ipa150101.zip");
		// File inputFile = new File("ipa150101.xml");
		int skip = 0;
		int limit = 10;

		PatentType patentType = new PatentTypeDetect().fromFileName(inputFile);

		DumpReader dumpReader;
		switch(patentType){
		case Greenbook:
			dumpReader = new DumpFileAps(inputFile);
			break;
		default:
			dumpReader = new DumpFileXml(inputFile);
		}

		dumpReader.open();
		if (skip > 0){
			dumpReader.skip(skip);
		}

		for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
		    String xmlDocStr = (String) dumpReader.next();
		    try(PatentReader patentReader = new PatentReader(xmlDocStr, patentType)){
		        Patent patent = patentReader.read();
		        System.out.println(patent.getDocumentId().toText());
		    }
		}
	}
}
