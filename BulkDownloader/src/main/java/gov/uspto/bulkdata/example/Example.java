package gov.uspto.bulkdata.example;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

import gov.uspto.bulkdata.BulkReaderArguments;
import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.RecordReader;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.patent.PatentDocFormat;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
//import gov.uspto.patent.serialize.JsonMapperFlat;
//import gov.uspto.patent.serialize.JsonMapperPATFT;
import gov.uspto.patent.serialize.JsonMapperStream;

public class Example implements RecordProcessor {

	private PatentReader patentReader;
	private DocumentBuilder<Patent> jsonBuilder;

	public Example(DocumentBuilder<Patent> jsonBuilder) {
		this.jsonBuilder = jsonBuilder;
	}

	@Override
	public void setPatentDocFormat(PatentDocFormat docFormat) {
		this.patentReader = new PatentReader(docFormat);
	}

	@Override
	public void initialize(Writer writer) throws IOException {
		System.out.println("--- START ---");
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws DocumentException, IOException {
		// writer.write(sourceTxt);
		// writer.write(rawRecord);

		try {
			Patent patent = patentReader.read(new StringReader(rawRecord));

			jsonBuilder.write(patent, writer);

		} catch (PatentReaderException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void finish(Writer writer) throws IOException {
		System.out.println("--- DONE ---");
	}

	public static void main(String[] args) throws PatentReaderException, IOException, DocumentException {

		Path inputBulkFile = Paths.get(args[0]);
		Path outputFile = Paths.get("example.txt");

		BulkReaderArguments config = new BulkReaderArguments();
		// config.parseArgs(args); // parse command-line args.
		config.setInputFile(inputBulkFile);
		config.setRecordReadLimit(1);
		config.setOutputFile(outputFile);

		RecordReader bulkReader = new RecordReader(config);

		DocumentBuilder<Patent> jsonBuilder = new JsonMapperStream(true);
		// DocumentBuilder<Patent> jsonBuilder = new JsonMapperFlat(true, false);
		// DocumentBuilder<Patent> jsonBuilder = new JsonMapperPATFT(true, false);

		RecordProcessor process = new Example(jsonBuilder);
		bulkReader.read(process);
	}

}
