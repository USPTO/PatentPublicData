package gov.uspto.bulkdata.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import gov.uspto.common.filter.FileFilterChain;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentDocFormatDetect;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.bulk.DumpFileAps;
import gov.uspto.patent.bulk.DumpFileXml;
import gov.uspto.patent.bulk.DumpReader;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapper;
import gov.uspto.patent.serialize.JsonMapperFlat;
import gov.uspto.patent.serialize.JsonMapperStream;

public class Example2 {

	private PatentReader patentReader;
	private JsonMapperStream jsonBuilder;

	public Example2(PatentReader patentReader) {
		this.patentReader = patentReader;
		this.jsonBuilder = new JsonMapperStream(true);
		// this.jsonBuilder = new JsonMapperFlat(true, false);
		// this.jsonBuilder = new JsonMapperPATFT(true, false);
	}

	public void run(DumpReader dumpReader, int limit, boolean writeFile) throws PatentReaderException, IOException {
		for (int i = 1; dumpReader.hasNext() && i <= limit; i++) {
			String xmlDocStr = (String) dumpReader.next();

			Patent patent = patentReader.read(new StringReader(xmlDocStr));
			String patentId = patent.getDocumentId().toText();

			System.out.println(patentId);
			// System.out.println("Patent Object: " + patent.toString());

			Writer writer;
			if (writeFile) {
				writer = new FileWriter(patentId + ".json");
			} else {
				writer = new StringWriter();
			}

			try {
				jsonBuilder.write(patent, writer);
				if (!writeFile) {
					System.out.println("JSON: " + writer.toString());
				}
			} catch (IOException e) {
				System.err.println("Failed to write file for: " + patentId + "\n" + e.getStackTrace());
			} finally {
				writer.close();
			}
		}

		dumpReader.close();
	}

	public static void main(String... args) throws IOException, PatentReaderException {

		File inputFile = new File(args[0]);
		int skip = 100;
		int limit = 1;
		boolean flatJson = false;
		boolean jsonPrettyPrint = true;
		boolean writeFile = false;

		PatentDocFormat patentDocFormat = new PatentDocFormatDetect().fromFileName(inputFile);

		DumpReader dumpReader;
		switch (patentDocFormat) {
		case Greenbook:
			dumpReader = new DumpFileAps(inputFile);
			break;
		default:
			dumpReader = new DumpFileXml(inputFile);
			FileFilterChain filters = new FileFilterChain();
			// filters.addRule(new PathFileFilter(""));
			filters.addRule(new SuffixFileFilter("xml"));
			dumpReader.setFileFilter(filters);
		}

		dumpReader.open();
		if (skip > 0) {
			dumpReader.skip(skip);
		}

		DocumentBuilder<Patent> json;
		if (flatJson) {
			json = new JsonMapperFlat(jsonPrettyPrint, false);
		} else {
			json = new JsonMapper(jsonPrettyPrint, false);
		}

		PatentReader patentReader = new PatentReader(dumpReader.getPatentDocFormat());
		
		Example2 process = new Example2(patentReader);
		process.run(dumpReader, limit, writeFile);
	}
}
