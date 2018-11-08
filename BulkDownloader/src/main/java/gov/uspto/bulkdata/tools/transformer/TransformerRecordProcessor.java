package gov.uspto.bulkdata.tools.transformer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.MDC;

import gov.uspto.bulkdata.RecordProcessor;
import gov.uspto.bulkdata.tools.grep.DocumentException;
import gov.uspto.bulkdata.tools.grep.GrepRecordProcessor;
import gov.uspto.common.io.DummyWriter;
import gov.uspto.patent.PatentReader;
import gov.uspto.patent.PatentReaderException;
import gov.uspto.patent.model.Patent;
import gov.uspto.patent.serialize.DocumentBuilder;
import gov.uspto.patent.serialize.JsonMapperFlat;
import gov.uspto.patent.serialize.JsonMapperPATFT;
import gov.uspto.patent.serialize.JsonMapperStream;
import gov.uspto.patent.serialize.PlainText;

public class TransformerRecordProcessor implements RecordProcessor {

	private final TransformerConfig config;
	private PatentReader patentReader;
	private String currentFilename;
	private Writer currentWriter;
	private List<RecordProcessor> preProcessors;
	private GrepRecordProcessor matchProcessor;

	public TransformerRecordProcessor(TransformerConfig config, PatentReader patentReader) {
		this.config = config;
		this.patentReader = patentReader;
	}

	@Override
	public void initialize(Writer writer) throws IOException, TransformerConfigurationException {
		// empty.
	}

	public void setMatchProcessor(GrepRecordProcessor grepProcessor) {
		this.matchProcessor = grepProcessor;
	}

	@Override
	public Boolean process(String sourceTxt, String rawRecord, Writer writer) throws IOException, DocumentException {
		MDC.put("DOCID", sourceTxt);

		if (matchProcessor != null && !matchProcessor.process(sourceTxt, rawRecord, new DummyWriter())) {
			//System.out.println("Skipping Record: " + sourceTxt);
			return false;
		} else {
			System.out.println("Matched Record: " + sourceTxt);
		}

		Patent patent;
		try {
			patent = patentReader.read(new StringReader(rawRecord));
		} catch (PatentReaderException e) {
			return false;
		}

		String patentId = patent.getDocumentId() != null ? patent.getDocumentId().toText() : "";
		MDC.put("DOCID", patentId);

		if (!config.isBulkOutput()) {
			String currentFileName = patentId + ".json";
			Writer currentWriter = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(config.getOutputDir().resolve(currentFileName).toFile(), true),
					StandardCharsets.UTF_8));
			writeOutputType(sourceTxt, patent, currentWriter);
			currentWriter.close();
		} else {
			String filename = sourceTxt.replaceFirst("\\.zip:\\d+$", "");
			if (!filename.equals(currentFilename)) {
				if (currentWriter != null) {
					currentWriter.close();
				}
				currentWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getOutputDir().resolve(filename+"."+config.getOutputType()+"l").toFile(), true),
						StandardCharsets.UTF_8));
				currentFilename = filename;
			}
			writeOutputType(sourceTxt, patent, currentWriter);
			currentWriter.write('\n');
			currentWriter.flush();
		}

		return true;
	}

	public void writeOutputType(String sourceText, Patent patent, Writer writer) throws IOException {
		Boolean prettyPrint = config.isPrettyPrint();

		if (config.isBulkKV()) {
			writer.write(patent.getDocumentId().toText()+":"+sourceText);
			writer.write("\t");
		}

		switch (config.getOutputType().toLowerCase()) {
		case "plaintext":
		case "text":
		case "txt":
			new PlainText(prettyPrint).write(patent, writer);
			break;
		case "json":
		case "js":
			// writer.write("Patent JSON:\n");
			JsonMapperStream fileBuilder = new JsonMapperStream(prettyPrint);
			fileBuilder.write(patent, writer);
			break;
		case "patft":
		case "apft":
			// writer.write("Patent JSON:\n");
			JsonMapperPATFT builderPatft = new JsonMapperPATFT(prettyPrint, false);
			builderPatft.write(patent, writer);
			break;
		case "json_flat":
		case "jsonflat":
		case "flatjson":
			// writer.write("Patent JSON FLAT:\n");
			DocumentBuilder<Patent> fileBuilder2 = new JsonMapperFlat(prettyPrint, false);
			fileBuilder2.write(patent, writer);
			break;
		case "object":
		case "obj":
			writer.write(patent.toString());
			break;
		default:
			throw new RuntimeException("Unknown output type: " + config.getOutputType().toLowerCase());
		}
	}

	@Override
	public void finish(Writer writer) throws IOException {
		if (currentWriter != null) {
			currentWriter.close();
		}

		/*
		 * if (totalCount >= totalLimit) {
		 * LOGGER.info("Process Complete, Total Record Limit Reached [{}]", totalCount);
		 * } else { LOGGER.info("Process Complete, Total Records [{}]", totalCount); }
		 */
	}

}
